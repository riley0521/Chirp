package com.rfcoding.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rfcoding.chat.database.entities.UnseenMessageEntity

@Dao
interface UnseenMessageDao {

    @Upsert
    suspend fun upsertUnseenMessages(unseenMessages: List<UnseenMessageEntity>)

    @Query(
        "SELECT * FROM unseen_messages WHERE chatId = :chatId"
    )
    suspend fun getAllByChatId(chatId: String): List<UnseenMessageEntity>
}