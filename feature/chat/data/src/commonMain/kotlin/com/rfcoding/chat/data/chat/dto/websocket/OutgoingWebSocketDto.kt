package com.rfcoding.chat.data.chat.dto.websocket

import com.rfcoding.chat.domain.models.ChatMessageType
import kotlinx.serialization.Serializable

enum class OutgoingWebSocketType {
    NEW_MESSAGE,
    USER_TYPING
}

sealed interface OutgoingWebSocketDto {

    @Serializable
    data class NewMessage(
        val messageId: String,
        val chatId: String,
        val content: String,
        val messageType: ChatMessageType
    ): OutgoingWebSocketDto

    @Serializable
    data class UserTyping(
        val userId: String,
        val chatId: String
    ): OutgoingWebSocketDto
}