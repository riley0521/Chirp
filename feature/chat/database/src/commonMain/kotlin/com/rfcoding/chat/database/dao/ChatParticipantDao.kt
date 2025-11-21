package com.rfcoding.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rfcoding.chat.database.entities.ChatParticipantEntity
import com.rfcoding.chat.database.model.UsernameOnly

@Dao
interface ChatParticipantDao {

    @Upsert
    suspend fun upsertParticipant(participant: ChatParticipantEntity)

    @Upsert
    suspend fun upsertParticipants(participants: List<ChatParticipantEntity>)

    @Query("SELECT * FROM chat_participants")
    suspend fun getAllParticipants(): List<ChatParticipantEntity>

    @Query("DELETE FROM chat_participants")
    suspend fun deleteAllParticipants()

    @Query("SELECT username FROM chat_participants WHERE userId IN (:userIds)")
    suspend fun getUsernamesByUserIds(userIds: List<String>): List<UsernameOnly>

    @Query("""
        UPDATE chat_participants
        SET profilePictureUrl = :profileUrl
        WHERE userId = :userId
    """)
    suspend fun updateProfilePicture(userId: String, profileUrl: String?)
}