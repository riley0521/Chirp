package com.rfcoding.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.rfcoding.chat.database.entities.ChatParticipantCrossRef
import com.rfcoding.chat.database.entities.ChatParticipantEntity

@Dao
interface ChatParticipantCrossRefDao {

    @Upsert
    suspend fun upsertCrossRefs(refs: List<ChatParticipantCrossRef>)

    @Query("SELECT userId FROM chat_participant_cross_ref WHERE chatId = :chatId AND userId = :userId")
    suspend fun getByChatAndParticipantId(chatId: String, userId: String): String?

    @Query("SELECT userId FROM chat_participant_cross_ref WHERE chatId = :chatId")
    suspend fun getParticipantIdsByChat(chatId: String): List<String>

    @Query("SELECT chatId FROM chat_participant_cross_ref WHERE userId = :userId")
    suspend fun getChatIdsByParticipant(userId: String): List<String>

    @Query("DELETE FROM chat_participant_cross_ref WHERE chatId = :chatId AND userId = :userId")
    suspend fun deleteByChatAndParticipantId(chatId: String, userId: String)

    @Query("DELETE FROM chat_participant_cross_ref WHERE chatId = :chatId AND userId != :userId")
    suspend fun deleteParticipantsExceptLocalByChatId(chatId: String, userId: String)

    @Transaction
    suspend fun syncChatParticipants(
        localUserId: String,
        chatId: String,
        participants: List<ChatParticipantEntity>
    ) {
        if (participants.isEmpty()) {
            return
        }

        /**
         * Delete all participants from a chat except the local user.
         * This way we can avoid complex logic like
         * resolving allParticipants, activeParticipants, and inactiveParticipants
         * who to add/remove.
         */
        deleteParticipantsExceptLocalByChatId(chatId, localUserId)

        val serverCrossRefs = (participants.map { it.userId }.toSet()).map { userId ->
            ChatParticipantCrossRef(
                chatId = chatId,
                userId = userId
            )
        }
        upsertCrossRefs(serverCrossRefs)
    }
}