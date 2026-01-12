package com.rfcoding.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rfcoding.chat.database.entities.ChatMediaEntity

@Dao
interface ChatMediaDao {

    @Upsert
    suspend fun upsertMedia(media: ChatMediaEntity)

    @Upsert
    suspend fun upsertMedias(medias: List<ChatMediaEntity>)

    @Query("SELECT * FROM chat_medias WHERE name = :name")
    suspend fun getByName(name: String): ChatMediaEntity?

    @Query("DELETE FROM chat_medias WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM chat_medias WHERE messageId = :messageId")
    suspend fun getByMessageId(messageId: String): List<ChatMediaEntity>

    @Query("UPDATE chat_medias SET status = 'SENDING' WHERE messageId = :messageId")
    suspend fun updateToSending(messageId: String)
}