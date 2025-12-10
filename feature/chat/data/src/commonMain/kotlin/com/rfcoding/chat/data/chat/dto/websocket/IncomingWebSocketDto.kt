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

sealed interface IncomingWebSocketDto {

    @Serializable
    data class NewMessage(
        val id: String,
        val chatId: String,
        val senderId: String?,
        val content: String,
        val messageType: ChatMessageType,
        val imageUrls: List<String>,
        val event: ChatMessageEventDto?,
        val createdAt: String,
        val audioDurationInSeconds: Int
    ): IncomingWebSocketDto

    @Serializable
    data class DeleteMessage(
        val chatId: String,
        val messageId: String
    ): IncomingWebSocketDto

    @Serializable
    data class ProfilePictureUpdated(
        val userId: String,
        val newProfilePictureUrl: String?
    ): IncomingWebSocketDto

    @Serializable
    data class UserTyping(
        val userId: String,
        val chatId: String
    ): IncomingWebSocketDto
}