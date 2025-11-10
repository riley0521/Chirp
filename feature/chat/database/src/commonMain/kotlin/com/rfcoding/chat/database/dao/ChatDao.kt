package com.rfcoding.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.rfcoding.chat.database.entities.ChatEntity
import com.rfcoding.chat.database.entities.ChatInfoEntity
import com.rfcoding.chat.database.entities.ChatParticipantCrossRef
import com.rfcoding.chat.database.entities.ChatParticipantEntity
import com.rfcoding.chat.database.entities.ChatWithParticipantsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Upsert
    suspend fun upsertChat(chat: ChatEntity)

    @Upsert
    suspend fun upsertChats(chats: List<ChatEntity>)

    @Query("DELETE FROM chats WHERE chatId = :chatId")
    suspend fun deleteChatById(chatId: String)

    @Query("SELECT * FROM chats ORDER BY lastActivityAt DESC")
    @Transaction
    fun getChatsWithParticipants(): Flow<List<ChatWithParticipantsEntity>>

    @Query("SELECT * FROM chats WHERE chatId = :chatId")
    @Transaction
    suspend fun getChatById(chatId: String): ChatWithParticipantsEntity?

    @Query("DELETE FROM chats")
    suspend fun deleteAllChats()

    @Query("SELECT chatId FROM chats")
    suspend fun getAllChatIds(): List<String>

    @Transaction
    suspend fun deleteChatsByIds(chatIds: List<String>) {
        chatIds.forEach { chatId ->
            deleteChatById(chatId)
        }
    }

    @Query("SELECT COUNT(*) FROM chats")
    fun getChatCount(): Flow<Int>

    @Query("""
        SELECT p.*
        FROM chat_participants p
        JOIN chat_participant_cross_ref ref ON p.userId = ref.userId
        WHERE ref.chatId = :chatId
        ORDER BY p.username
    """)
    fun getParticipantsByChatId(chatId: String): Flow<List<ChatParticipantEntity>>

    @Query("SELECT * FROM chats WHERE chatId = :chatId")
    @Transaction
    fun getChatInfoById(chatId: String): Flow<ChatInfoEntity?>

    @Transaction
    suspend fun upsertChatWithParticipantsAndCrossRefs(
        localUserId: String,
        chat: ChatEntity,
        participants: List<ChatParticipantEntity>,
        participantDao: ChatParticipantDao,
        crossRefDao: ChatParticipantCrossRefDao
    ) {
        if (participants.isEmpty()) {
            return
        }

        val crossRefs = participants.map { participant ->
            ChatParticipantCrossRef(
                chatId = chat.chatId,
                userId = participant.userId
            )
        }

        upsertChat(chat)
        participantDao.upsertParticipants(participants)

        // If the cross ref is already existing, we will just sync it.
        if (
            crossRefDao.getByChatAndParticipantId(
                chatId = chat.chatId,
                userId = localUserId
            ) != null
        ) {
            crossRefDao.syncChatParticipants(
                localUserId = localUserId,
                chatId = chat.chatId,
                participants = participants
            )
        } else {
            crossRefDao.upsertCrossRefs(crossRefs)
        }
    }

    @Transaction
    suspend fun upsertChatsWithParticipantsAndCrossRefs(
        localUserId: String,
        chats: List<ChatWithParticipantsEntity>,
        participantDao: ChatParticipantDao,
        crossRefDao: ChatParticipantCrossRefDao
    ) {
        upsertChats(chats.map { it.chat })
        val allParticipants = chats.flatMap { it.participants }.filterNotNull()
        participantDao.upsertParticipants(allParticipants)

        val crossRefs = chats.flatMap { chatsWithParticipants ->
            chatsWithParticipants.participants.mapNotNull { participant ->
                if (participant == null) {
                    return@mapNotNull null
                }

                ChatParticipantCrossRef(
                    chatId = chatsWithParticipants.chat.chatId,
                    userId = participant.userId
                )
            }
        }
        crossRefDao.upsertCrossRefs(crossRefs)

        chats.forEach { chat ->
            crossRefDao.syncChatParticipants(
                localUserId = localUserId,
                chatId = chat.chat.chatId,
                participants = chat.participants.filterNotNull()
            )
        }
    }
}