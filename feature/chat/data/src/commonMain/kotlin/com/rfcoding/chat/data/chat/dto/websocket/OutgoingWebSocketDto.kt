package com.rfcoding.chat.data.chat.dto.websocket

import com.rfcoding.chat.domain.models.ChatMessageType
import kotlinx.serialization.Serializable

enum class OutgoingWebSocketType {
    NEW_MESSAGE,
    USER_TYPING
}

@Serializable
sealed class OutgoingWebSocketDto(
    val type: OutgoingWebSocketType
) {

    data class NewMessage(
        val messageId: String,
        val chatId: String,
        val content: String,
        val messageType: ChatMessageType = ChatMessageType.MESSAGE_TEXT
    ): OutgoingWebSocketDto(OutgoingWebSocketType.NEW_MESSAGE)

    data class UserTyping(
        val userId: String,
        val chatId: String
    ): OutgoingWebSocketDto(OutgoingWebSocketType.USER_TYPING)
}