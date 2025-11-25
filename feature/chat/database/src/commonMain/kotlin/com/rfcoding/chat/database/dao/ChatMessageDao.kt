package com.rfcoding.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.rfcoding.chat.database.entities.ChatMessageEntity
import com.rfcoding.chat.database.entities.MessageWithSenderEntity
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

@Dao
interface ChatMessageDao {

    @Upsert
    suspend fun upsertMessage(message: ChatMessageEntity)

    @Upsert
    suspend fun upsertMessages(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)

    @Query("DELETE FROM chat_messages WHERE id IN (:ids)")
    suspend fun deleteMessageByIds(ids: List<String>)

    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY createdAt DESC")
    fun getMessagesByChatId(chatId: String): Flow<List<MessageWithSenderEntity>>

    @Query("""
        SELECT * 
        FROM chat_messages 
        WHERE chatId = :chatId 
        ORDER BY createdAt DESC
        LIMIT :limit
    """)
    suspend fun getMessagesByChatIdLimited(chatId: String, limit: Int): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages WHERE id = :id")
    suspend fun getMessageById(id: String): ChatMessageEntity?

    @Query("""
        UPDATE chat_messages 
        SET deliveryStatus = :deliveryStatus, deliveredAt = :deliveredAt 
        WHERE id = :id
    """)
    suspend fun updateDeliveryStatus(id: String, deliveryStatus: ChatMessageDeliveryStatus, deliveredAt: Instant)

    @Transaction
    suspend fun upsertMessagesAndSyncIfNecessary(
        chatId: String,
        serverMessages: List<ChatMessageEntity>,
        pageSize: Int,
        shouldSync: Boolean = false
    ) {
        val localMessages = getMessagesByChatIdLimited(chatId, pageSize)
        upsertMessages(serverMessages)

        if (!shouldSync) {
            return
        }

        val serverIds = serverMessages.map { it.id }.toSet()
        val messagesToDelete = localMessages.filter { localMessage ->
            val missingOnServer = localMessage.id !in serverIds
            val isSent = localMessage.deliveryStatus == ChatMessageDeliveryStatus.SENT

            missingOnServer && isSent
        }

        val messageIds = messagesToDelete.map { it.id }
        deleteMessageByIds(messageIds)
    }
}