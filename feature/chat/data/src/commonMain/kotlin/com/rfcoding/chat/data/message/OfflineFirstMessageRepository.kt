package com.rfcoding.chat.data.message

import com.rfcoding.chat.data.chat.dto.websocket.OutgoingWebSocketType
import com.rfcoding.chat.data.chat.dto.websocket.WebSocketMessageDto
import com.rfcoding.chat.data.mappers.toDto
import com.rfcoding.chat.data.network.KtorWebSocketConnector
import com.rfcoding.chat.database.ChirpChatDatabase
import com.rfcoding.chat.database.entities.ChatMessageEntity
import com.rfcoding.chat.database.mapper.toDomain
import com.rfcoding.chat.database.mapper.toEntity
import com.rfcoding.chat.domain.message.ChatMessageService
import com.rfcoding.chat.domain.message.MessageRepository
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.domain.models.ChatMessageType
import com.rfcoding.chat.domain.models.MessageWithSender
import com.rfcoding.chat.domain.models.OutgoingNewMessage
import com.rfcoding.core.data.database.safeDatabaseUpdate
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
import com.rfcoding.core.domain.util.Result
import com.rfcoding.core.domain.util.asEmptyResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.json.Json
import kotlin.time.Clock

class OfflineFirstMessageRepository(
    private val connector: KtorWebSocketConnector,
    private val chatMessageService: ChatMessageService,
    private val chatDb: ChirpChatDatabase,
    private val sessionStorage: SessionStorage,
    private val json: Json,
    private val applicationScope: CoroutineScope
): MessageRepository {

    override suspend fun fetchMessages(
        chatId: String,
        before: String?
    ): EmptyResult<DataError> = coroutineScope {
        when (val result = chatMessageService.fetchMessages(chatId, before)) {
            is Result.Failure -> result
            is Result.Success -> {
                val chatEntities = result.data.map { (message, affectedUserIds) ->
                    message.toEntity(affectedUserIds.orEmpty())
                }
                val updateResult = safeDatabaseUpdate {
                    chatDb.chatMessageDao.upsertMessagesAndSyncIfNecessary(
                        chatId = chatId,
                        serverMessages = chatEntities,
                        // Server response of messages may differ in size in the future.
                        // So, this is for safer approach.
                        pageSize = chatEntities.size,
                        shouldSync = before == null // Only sync for most recent page
                    )
                }
                updateResult.asEmptyResult()
            }
        }
    }

    override fun getMessagesForChat(chatId: String): Flow<List<MessageWithSender>> {
        return chatDb.chatMessageDao.getMessagesByChatId(chatId).map { messages ->
            supervisorScope {
                val mappedMessages = messages.map { messageWithSender ->
                    async {
                        val affectedUsernames = chatDb.chatParticipantDao.getUsernamesByUserIds(
                            messageWithSender.message.event?.affectedUserIds.orEmpty()
                        ).map { it.username }
                        messageWithSender.toDomain(affectedUsernames)
                    }
                }

                mappedMessages.awaitAll()
            }
        }
    }

    private suspend fun getUserId(): String {
        return sessionStorage
            .observeAuthenticatedUser()
            .firstOrNull()
            ?.user?.id ?: throw IllegalStateException("User is not logged in.")
    }

    override suspend fun sendMessage(message: OutgoingNewMessage): EmptyResult<DataError> {
        val newMessage = ChatMessageEntity(
            id = message.messageId,
            chatId = message.chatId,
            senderId = getUserId(),
            content = message.content,
            chatMessageType = ChatMessageType.MESSAGE_TEXT,
            imageUrls = emptyList(),
            event = null,
            createdAt = Clock.System.now(),
            deliveryStatus = ChatMessageDeliveryStatus.SENDING
        )

        val localResult = safeDatabaseUpdate {
            chatDb.chatMessageDao.upsertMessage(newMessage)
        }
        if (localResult is Result.Failure) {
            return localResult
        }

        val rawMessage = json.encodeToString(
            WebSocketMessageDto(
                type = OutgoingWebSocketType.NEW_MESSAGE.name,
                payload = json.encodeToString(message.toDto())
            )
        )

        return when (val result = connector.sendMessage(rawMessage)) {
            is Result.Failure -> {
                applicationScope.launch {
                    chatDb.chatMessageDao.updateDeliveryStatus(
                        id = message.messageId,
                        deliveryStatus = ChatMessageDeliveryStatus.FAILED,
                        deliveredAt = Clock.System.now()
                    )
                }.join()

                result
            }
            is Result.Success -> {
                chatDb.chatMessageDao.updateDeliveryStatus(
                    id = message.messageId,
                    deliveryStatus = ChatMessageDeliveryStatus.SENT,
                    deliveredAt = Clock.System.now()
                )

                result
            }
        }
    }

    override suspend fun retryMessage(messageId: String): EmptyResult<DataError> {
        return safeDatabaseUpdate {
            val localMessage = chatDb.chatMessageDao.getUnsentMessageById(messageId)
                ?: return Result.Failure(DataError.Local.NOT_FOUND)

            chatDb.chatMessageDao.updateDeliveryStatus(
                id = messageId,
                deliveryStatus = ChatMessageDeliveryStatus.SENDING,
                deliveredAt = Clock.System.now()
            )

            val outgoingMessage = OutgoingNewMessage(
                messageId = messageId,
                chatId = localMessage.chatId,
                content = localMessage.content
            )
            val rawMessage = json.encodeToString(
                WebSocketMessageDto(
                    type = OutgoingWebSocketType.NEW_MESSAGE.name,
                    payload = json.encodeToString(outgoingMessage.toDto())
                )
            )

            return when (val result = connector.sendMessage(rawMessage)) {
                is Result.Failure -> {
                    applicationScope.launch {
                        chatDb.chatMessageDao.updateDeliveryStatus(
                            id = messageId,
                            deliveryStatus = ChatMessageDeliveryStatus.FAILED,
                            deliveredAt = Clock.System.now()
                        )
                    }.join()

                    result
                }
                is Result.Success -> {
                    chatDb.chatMessageDao.updateDeliveryStatus(
                        id = messageId,
                        deliveryStatus = ChatMessageDeliveryStatus.SENT,
                        deliveredAt = Clock.System.now()
                    )

                    result
                }
            }
        }
    }
}