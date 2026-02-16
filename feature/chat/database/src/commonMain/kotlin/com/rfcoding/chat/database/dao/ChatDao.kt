package com.rfcoding.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.rfcoding.chat.database.entities.ChatEntity
import com.rfcoding.chat.database.entities.ChatInfoEntity
import com.rfcoding.chat.database.entities.ChatMessageEntity
import com.rfcoding.chat.database.entities.ChatParticipantCrossRef
import com.rfcoding.chat.database.entities.ChatParticipantEntity
import com.rfcoding.chat.database.entities.ChatWithParticipantsEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

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
    fun getChatWithParticipants(chatId: String): Flow<ChatWithParticipantsEntity?>

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

    @Query("""
        UPDATE chats
        SET lastActivityAt = :lastActivityAt
        WHERE chatId = :chatId
    """)
    suspend fun updateLastActivity(chatId: String, lastActivityAt: Instant)

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
        chat: ChatWithParticipantsEntity,
        participantDao: ChatParticipantDao,
        crossRefDao: ChatParticipantCrossRefDao,
        messageDao: ChatMessageDao
    ) {
        upsertChat(chat.chat)
        chat.lastMessage?.run {
            messageDao.upsertMessage(
                ChatMessageEntity(
                    id = id,
                    chatId = chatId,
                    senderId = senderId,
                    content = content,
                    chatMessageType = chatMessageType,
                    imageUrls = imageUrls,
                    event = event,
                    createdAt = createdAt,
                    deliveryStatus = deliveryStatus
                )
            )
        }

        // Update participants because maybe they've already updated their profile image.
        participantDao.upsertParticipants(chat.participants.filterNotNull())

        // Upsert cross refs and sync.
        val crossRefs = chat.participants.mapNotNull { participant ->
            if (participant == null) {
                return@mapNotNull null
            }

            ChatParticipantCrossRef(
                chatId = chat.chat.chatId,
                userId = participant.userId
            )
        }
        crossRefDao.upsertCrossRefs(crossRefs)
        crossRefDao.syncChatParticipants(
            localUserId = localUserId,
            chatId = chat.chat.chatId,
            participants = chat.participants.filterNotNull()
        )
    }

    @Transaction
    suspend fun upsertChatsWithParticipantsAndCrossRefs(
        localUserId: String,
        chats: List<ChatWithParticipantsEntity>,
        participantDao: ChatParticipantDao,
        crossRefDao: ChatParticipantCrossRefDao,
        messageDao: ChatMessageDao
    ) {
        // Update chats and insert the last message.
        upsertChats(chats.map { it.chat })
        chats.forEach { chat ->
            chat.lastMessage?.run {
                val message = ChatMessageEntity(
                    id = id,
                    chatId = chatId,
                    senderId = senderId,
                    content = content,
                    chatMessageType = chatMessageType,
                    imageUrls = imageUrls,
                    event = event,
                    createdAt = createdAt,
                    deliveryStatus = deliveryStatus
                )
                messageDao.upsertMessage(message)
            }
        }

        // Insert not null participants.
        val allParticipants = chats.flatMap { it.participants }.filterNotNull()
        participantDao.upsertParticipants(allParticipants)

        // START - Mapping all participants to ChatParticipantCrossRef and upserting it.
        // Then syncing chat participants with ChatParticipantCrossRef table.
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
        // END
    }
}