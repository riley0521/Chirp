package com.rfcoding.chat.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(
    tableName = "chats"
)
data class ChatEntity(
    @PrimaryKey
    val chatId: String,
    val lastMessageId: String?,
    val isGroupChat: Boolean,
    val name: String?,
    val creatorId: String?,
    val lastActivityAt: Instant,
    val createdAt: Instant
)
