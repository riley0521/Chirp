package com.rfcoding.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.rfcoding.chat.database.entities.ChatEntity
import com.rfcoding.chat.database.entities.ChatInfoEntity
import com.rfcoding.chat.database.entities.ChatParticipantEntity
import com.rfcoding.chat.database.entities.ChatWithParticipants
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
    fun getChatsWithParticipants(): Flow<List<ChatWithParticipants>>

    @Query("SELECT * FROM chats WHERE chatId = :chatId")
    suspend fun getChatById(chatId: String): ChatWithParticipants?

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
        WHERE ref.chatId = :chatId AND ref.isActive = true
        ORDER BY p.username
    """)
    fun getActiveParticipantsByChatId(chatId: String): Flow<List<ChatParticipantEntity>>

    @Query("SELECT * FROM chats WHERE chatId = :chatId")
    fun getChatInfoById(chatId: String): Flow<ChatInfoEntity?>
}