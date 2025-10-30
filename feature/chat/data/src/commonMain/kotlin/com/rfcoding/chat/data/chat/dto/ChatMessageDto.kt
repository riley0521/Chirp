package com.rfcoding.chat.data.chat.dto

import com.rfcoding.chat.domain.models.ChatMessageType
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageDto(
    val id: String,
    val chatId: String,
    val senderId: String?,
    val content: String,
    val messageType: ChatMessageType,
    val imageUrls: List<String>,
    val event: ChatMessageEventDto?,
    val createdAt: String
)
