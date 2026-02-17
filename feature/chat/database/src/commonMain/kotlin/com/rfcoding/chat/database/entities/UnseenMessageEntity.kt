package com.rfcoding.chat.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(
    tableName = "unseen_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["chatId"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UnseenMessageEntity(
    @PrimaryKey
    val messageId: String,
    val chatId: String,
    val createdAt: Instant
)
