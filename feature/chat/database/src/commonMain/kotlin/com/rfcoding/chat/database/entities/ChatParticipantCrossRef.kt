package com.rfcoding.chat.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "chat_participant_cross_ref",
    primaryKeys = ["chatId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["chatId"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ChatParticipantEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ChatParticipantCrossRef(
    val chatId: String,
    val userId: String,
    val isActive: Boolean
)
