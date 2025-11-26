package com.rfcoding.chat.data.chat

import com.rfcoding.chat.data.chat.dto.websocket.IncomingWebSocketDto
import com.rfcoding.chat.data.chat.dto.websocket.OutgoingWebSocketDto
import com.rfcoding.chat.data.chat.dto.websocket.OutgoingWebSocketType
import com.rfcoding.chat.data.chat.dto.websocket.WebSocketMessageDto
import com.rfcoding.chat.data.mappers.toDomain
import com.rfcoding.chat.data.mappers.toIncomingWebSocketDto
import com.rfcoding.chat.data.network.KtorWebSocketConnector
import com.rfcoding.chat.database.ChirpChatDatabase
import com.rfcoding.chat.database.entities.ChatMessageEntity
import com.rfcoding.chat.database.model.ChatMessageEventSerializable
import com.rfcoding.chat.domain.chat.ChatConnectionClient
import com.rfcoding.chat.domain.chat.ChatRepository
import com.rfcoding.chat.domain.chat.UserTypingData
import com.rfcoding.chat.domain.models.ChatMessage
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.domain.models.ChatMessageType
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.domain.logging.ChirpLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Instant

class WebSocketChatConnectionClient(
    private val connector: KtorWebSocketConnector,
    private val chatRepository: ChatRepository,
    private val chatDb: ChirpChatDatabase,
    private val sessionStorage: SessionStorage,
    private val json: Json,
    private val applicationScope: CoroutineScope,
    private val logger: ChirpLogger
): ChatConnectionClient {

    companion object {
        private const val USER_TYPE_DELAY_MILLIS = 3_000L
    }

    override val chatMessages: Flow<ChatMessage> = connector
        .messages
        .mapNotNull { parseIncomingMessage(it) }
        .filterIsInstance<IncomingWebSocketDto.NewMessage>()
        .map { newMessage ->
            newMessage.toDomain(
                fetchUsernames = { userIds ->
                    chatDb
                        .chatParticipantDao
                        .getUsernamesByUserIds(userIds)
                        .map { it.username }
                }
            )
        }
        .shareIn(
            applicationScope,
            SharingStarted.WhileSubscribed(5_000L)
        )

    override val connectionState = connector.connectionState

    private var lastType = Clock.System.now()
    private val _usersTypingState = MutableStateFlow<Map<String, Map<String, UserTypingData>>>(mapOf())
    override val usersTypingState = _usersTypingState
        .onStart {
            scheduleRemovalOfUserTyping()
        }
        .map { curState ->
            curState.values.flatMap { it.values }
        }
        .onEach { userList ->
            val str = userList.joinToString("\n") { "User: ${it.userId} @ Room: ${it.chatId}" }
            logger.debug(str)
        }
        .stateIn(
            applicationScope,
            SharingStarted.WhileSubscribed(5_000L),
            emptyList()
        )
    private val mutex = Mutex()

    override suspend fun sendTypingIndicator(chatId: String) {
        val userId = sessionStorage
            .observeAuthenticatedUser()
            .firstOrNull()
            ?.user?.id ?: return

        val now = Clock.System.now()
        val diff = now - lastType

        // If the user is typing, we want to wait for a while before sending a web socket message again.
        if (diff.inWholeMilliseconds < 500L) {
            return
        }

        // Update lastType timestamp to now.
        lastType = now
        val typingData = OutgoingWebSocketDto.UserTyping(
            userId = userId,
            chatId = chatId
        )
        val webSocketMessage = json.encodeToString(
            WebSocketMessageDto(
                type = OutgoingWebSocketType.USER_TYPING.name,
                payload = json.encodeToString(typingData)
            )
        )

        // We don't care about the result.
        connector.sendMessage(webSocketMessage)
    }

    private suspend fun parseIncomingMessage(messageDto: WebSocketMessageDto): IncomingWebSocketDto? {
        val result = messageDto.toIncomingWebSocketDto(json)

        when (result) {
            is IncomingWebSocketDto.NewMessage -> handleNewMessage(result)
            is IncomingWebSocketDto.DeleteMessage -> deleteMessage(result.messageId)
            is IncomingWebSocketDto.ProfilePictureUpdated -> updateProfilePicture(
                userId = result.userId,
                profileUrl = result.newProfilePictureUrl
            )
            is IncomingWebSocketDto.UserTyping -> handleUserTyping(
                userId = result.userId,
                chatId = result.chatId
            )
            else -> Unit
        }

        return result
    }

    private suspend fun handleNewMessage(message: IncomingWebSocketDto.NewMessage) {
        val chatExists = chatDb.chatDao.getChatById(message.chatId) != null
        if (!chatExists) {
            chatRepository.fetchChatById(message.chatId)
        }

        chatDb.chatMessageDao.upsertMessage(
            ChatMessageEntity(
                id = message.id,
                chatId = message.chatId,
                senderId = message.senderId,
                content = message.content,
                chatMessageType = message.messageType,
                imageUrls = message.imageUrls,
                event = message.event?.let {
                    ChatMessageEventSerializable(
                        affectedUserIds = it.affectedUserIds,
                        type = it.type
                    )
                },
                createdAt = Instant.parse(message.createdAt),
                deliveryStatus = ChatMessageDeliveryStatus.SENT,
                deliveredAt = Clock.System.now()
            )
        )

        if (message.event != null && message.messageType == ChatMessageType.MESSAGE_EVENT) {
            refreshChat(message.chatId)
        } else {
            chatDb.chatDao.updateLastActivity(
                chatId = message.chatId,
                lastActivityAt = Clock.System.now()
            )
        }
    }

    private suspend fun refreshChat(chatId: String) {
        chatRepository.fetchChatById(chatId)
    }

    private suspend fun deleteMessage(messageId: String) {
        chatDb.chatMessageDao.deleteMessageById(messageId)
    }

    private suspend fun updateProfilePicture(userId: String, profileUrl: String?) {
        chatDb.chatParticipantDao.updateProfilePicture(userId, profileUrl)

        val authInfo = sessionStorage.observeAuthenticatedUser().firstOrNull()
        if (authInfo != null && authInfo.user != null) {
            if (authInfo.user!!.id != userId) {
                return
            }

            sessionStorage.set(
                authInfo.copy(
                    user = authInfo.user?.copy(
                        profileImageUrl = profileUrl
                    )
                )
            )
        }
    }

    private suspend fun handleUserTyping(userId: String, chatId: String) {
        mutex.withLock {
            val curState = _usersTypingState.value.toMutableMap()
            val usersInChat = curState[chatId]?.toMutableMap()
            if (usersInChat == null) {
                curState.put(chatId, mapOf(userId to UserTypingData(userId, chatId)))
            } else {
                usersInChat.put(userId, UserTypingData(userId, chatId))
                curState.put(chatId, usersInChat)
            }

            _usersTypingState.update { curState }
        }
    }

    private fun scheduleRemovalOfUserTyping() {
        applicationScope.launch {
            while(true) {
                mutex.withLock {
                    val now = Clock.System.now()
                    val curState = _usersTypingState.value.toMutableMap()

                    val updatedMap = mutableMapOf<String, Map<String, UserTypingData>>()
                    curState.entries.forEach { (key, usersInChat) ->
                        val validUsers = usersInChat.filterValues {
                            val diff = now - it.typedAt
                            diff.inWholeMilliseconds < USER_TYPE_DELAY_MILLIS
                        }

                        updatedMap.put(key, validUsers)
                    }

                    _usersTypingState.update { updatedMap }
                }

                delay(USER_TYPE_DELAY_MILLIS)
            }
        }
    }
}