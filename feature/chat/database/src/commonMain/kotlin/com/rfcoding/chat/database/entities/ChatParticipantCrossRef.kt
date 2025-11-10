package com.rfcoding.chat.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

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
            childColumns = ["userId"]
        )
    ],
    indices = [
        Index(value = ["chatId"]),
        Index(value = ["userId"])
    ]
)
data class ChatParticipantCrossRef(
    val chatId: String,
    val userId: String
)
