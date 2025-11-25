package com.rfcoding.chat.data.message

import com.rfcoding.chat.database.ChirpChatDatabase
import com.rfcoding.chat.database.mapper.toDomain
import com.rfcoding.chat.database.mapper.toEntity
import com.rfcoding.chat.domain.message.ChatMessageService
import com.rfcoding.chat.domain.message.MessageRepository
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.domain.models.MessageWithSender
import com.rfcoding.core.data.database.safeDatabaseUpdate
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
import com.rfcoding.core.domain.util.Result
import com.rfcoding.core.domain.util.asEmptyResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope
import kotlin.time.Clock

class OfflineFirstMessageRepository(
    private val chatMessageService: ChatMessageService,
    private val chatDb: ChirpChatDatabase
): MessageRepository {

    override suspend fun updateMessageDeliveryStatus(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate {
            chatDb.chatMessageDao.updateDeliveryStatus(
                id = messageId,
                deliveryStatus = status,
                deliveredAt = Clock.System.now()
            )
        }
    }

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
}