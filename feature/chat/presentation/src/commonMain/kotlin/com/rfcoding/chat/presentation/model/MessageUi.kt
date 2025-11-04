package com.rfcoding.chat.presentation.model

import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.domain.models.ChatMessageEventType
import com.rfcoding.chat.domain.models.ChatMessageType
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.presentation.util.UiText

sealed interface MessageUi {
    data class LocalUserMessage(
        val id: String,
        val content: String,
        val deliveryStatus: ChatMessageDeliveryStatus,
        val isMenuOpen: Boolean,
        val formattedSentTime: UiText,
        val imageUrls: List<String> = emptyList(),
        val messageType: ChatMessageType = ChatMessageType.MESSAGE_TEXT
    ): MessageUi

    data class OtherUserMessage(
        val id: String,
        val content: String,
        val sender: ChatParticipantUi?,
        val formattedSentTime: UiText,
        val imageUrls: List<String> = emptyList(),
        val messageType: ChatMessageType = ChatMessageType.MESSAGE_TEXT
    ): MessageUi

    data class DateSeparator(
        val id: String,
        val date: UiText
    ): MessageUi

    data class EventMessage(
        val id: String,
        val type: ChatMessageEventType,
        val username: String,
        val affectedUsernames: List<String>
    ): MessageUi
}