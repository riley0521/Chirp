package com.rfcoding.chat.presentation.model

import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.domain.models.ChatMessageEventType
import com.rfcoding.chat.domain.models.ChatMessageType
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.presentation.util.UiText

sealed class MessageUi(open val id: String) {
    data class LocalUserMessage(
        override val id: String,
        val content: String,
        val deliveryStatus: ChatMessageDeliveryStatus,
        val formattedSentTime: UiText,
        val media: MediaUi,
        val messageType: ChatMessageType = ChatMessageType.MESSAGE_TEXT,
        val audioDurationInSeconds: Int = 0
    ): MessageUi(id)

    data class OtherUserMessage(
        override val id: String,
        val content: String,
        val sender: ChatParticipantUi?,
        val formattedSentTime: UiText,
        val media: MediaUi,
        val messageType: ChatMessageType = ChatMessageType.MESSAGE_TEXT,
        val audioDurationInSeconds: Int = 0
    ): MessageUi(id)

    data class DateSeparator(
        override val id: String,
        val date: UiText
    ): MessageUi(id)

    data class EventMessage(
        override val id: String,
        val type: ChatMessageEventType,
        val username: String?,
        val affectedUsernames: List<String?>
    ): MessageUi(id)
}