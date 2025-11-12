package com.rfcoding.chat.database.view

import androidx.room.DatabaseView
import com.rfcoding.chat.database.model.ChatMessageEventSerializable
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.domain.models.ChatMessageType
import kotlin.time.Instant

@DatabaseView(
    viewName = "last_message_view_per_chat",
    value = """
        SELECT m1.*
        FROM chat_messages m1
        JOIN (
            SELECT chatId, MAX(createdAt) AS lastActivityAt
            FROM chat_messages
            GROUP BY chatId
        ) m2 ON m1.chatId = m2.chatId AND m1.createdAt = m2.lastActivityAt
    """
)
data class LastMessageView(
    val id: String,
    val chatId: String,
    val senderId: String?,
    val content: String,
    val chatMessageType: ChatMessageType,
    val imageUrls: List<String>,
    val event: ChatMessageEventSerializable?,
    val createdAt: Instant,
    val deliveryStatus: ChatMessageDeliveryStatus
)
