package com.rfcoding.chat.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_participants"
)
data class ChatParticipantEntity(
    @PrimaryKey
    val userId: String,
    val username: String,
    val email: String,
    val profilePictureUrl: String?
)
