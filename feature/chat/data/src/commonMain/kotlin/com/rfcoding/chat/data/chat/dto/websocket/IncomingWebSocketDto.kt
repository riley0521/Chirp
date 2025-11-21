package com.rfcoding.chat.data.chat.dto.websocket

import com.rfcoding.chat.data.chat.dto.ChatMessageEventDto
import com.rfcoding.chat.domain.models.ChatMessageType
import kotlinx.serialization.Serializable

enum class IncomingWebSocketType {
    NEW_MESSAGE,
    MESSAGE_DELETED,
    PROFILE_PICTURE_UPDATED,
    USER_TYPING
}

@Serializable
sealed class IncomingWebSocketDto(
    val type: IncomingWebSocketType
) {

    @Serializable
    data class NewMessage(
        val id: String,
        val chatId: String,
        val senderId: String?,
        val content: String,
        val messageType: ChatMessageType,
        val imageUrls: List<String>,
        val event: ChatMessageEventDto?,
        val createdAt: String
    ): IncomingWebSocketDto(IncomingWebSocketType.NEW_MESSAGE)

    @Serializable
    data class DeleteMessage(
        val chatId: String,
        val messageId: String
    ): IncomingWebSocketDto(IncomingWebSocketType.MESSAGE_DELETED)

    @Serializable
    data class ProfilePictureUpdated(
        val userId: String,
        val newProfilePictureUrl: String?
    ): IncomingWebSocketDto(IncomingWebSocketType.PROFILE_PICTURE_UPDATED)

    @Serializable
    data class UserTyping(
        val userId: String,
        val chatId: String
    ): IncomingWebSocketDto(IncomingWebSocketType.USER_TYPING)
}