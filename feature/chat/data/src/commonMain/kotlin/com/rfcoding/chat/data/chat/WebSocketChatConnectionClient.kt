package com.rfcoding.chat.data.chat

import com.rfcoding.chat.data.chat.dto.websocket.IncomingWebSocketDto
import com.rfcoding.chat.data.chat.dto.websocket.OutgoingWebSocketDto
import com.rfcoding.chat.data.chat.dto.websocket.WebSocketMessageDto
import com.rfcoding.chat.data.mappers.toDomain
import com.rfcoding.chat.data.mappers.toIncomingWebSocketDto
import com.rfcoding.chat.data.network.KtorWebSocketConnector
import com.rfcoding.chat.database.ChirpChatDatabase
import com.rfcoding.chat.database.entities.ChatMessageEntity
import com.rfcoding.chat.database.model.ChatMessageEventSerializable
import com.rfcoding.chat.domain.chat.ChatConnectionClient
import com.rfcoding.chat.domain.chat.ChatRepository
import com.rfcoding.chat.domain.chat.SendMessage
import com.rfcoding.chat.domain.error.ConnectionError
import com.rfcoding.chat.domain.message.MessageRepository
import com.rfcoding.chat.domain.models.ChatMessage
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.domain.models.ChatMessageType
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.domain.util.EmptyResult
import com.rfcoding.core.domain.util.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.json.Json
import kotlin.time.Instant

class WebSocketChatConnectionClient(
    private val connector: KtorWebSocketConnector,
    private val chatRepository: ChatRepository,
    private val chatDb: ChirpChatDatabase,
    private val sessionStorage: SessionStorage,
    private val messageRepository: MessageRepository,
    private val json: Json,
    private val applicationScope: CoroutineScope
): ChatConnectionClient {

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

    override suspend fun sendMessage(message: SendMessage): EmptyResult<ConnectionError> {
        val newMessage = OutgoingWebSocketDto.NewMessage(
            messageId = message.id,
            chatId = message.chatId,
            content = message.content
        )
        val webSocketMessage = json.encodeToString(
            WebSocketMessageDto(
                type = newMessage.type.name,
                payload = json.encodeToString(newMessage)
            )
        )

        return when (val result = connector.sendMessage(webSocketMessage)) {
            is Result.Failure -> {
                messageRepository.updateMessageDeliveryStatus(
                    messageId = message.id,
                    status = ChatMessageDeliveryStatus.FAILED
                )
                result
            }
            is Result.Success -> {
                result
            }
        }
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
            is IncomingWebSocketDto.UserTyping -> {} // TODO
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
                deliveryStatus = ChatMessageDeliveryStatus.SENT
            )
        )

        if (message.event != null && message.messageType == ChatMessageType.MESSAGE_EVENT) {
            refreshChat(message.chatId)
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
}