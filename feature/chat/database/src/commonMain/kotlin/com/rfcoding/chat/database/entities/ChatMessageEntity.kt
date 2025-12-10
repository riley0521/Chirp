package com.rfcoding.chat.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rfcoding.chat.database.model.ChatMessageEventSerializable
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.domain.models.ChatMessageType
import kotlin.time.Instant

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["chatId"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["chatId"]),
        Index(value = ["createdAt"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val chatId: String,
    val senderId: String?,
    val content: String,
    val chatMessageType: ChatMessageType,
    val imageUrls: List<String>,
    val event: ChatMessageEventSerializable?,
    val createdAt: Instant,
    val deliveryStatus: ChatMessageDeliveryStatus,
    val deliveredAt: Instant = createdAt,
    val audioDurationInSeconds: Int = 0
)
