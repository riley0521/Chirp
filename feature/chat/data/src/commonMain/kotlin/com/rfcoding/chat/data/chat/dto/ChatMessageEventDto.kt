package com.rfcoding.chat.data.chat.dto

import com.rfcoding.chat.domain.models.ChatMessageEventType
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageEventDto(
    val affectedUserIds: List<String>,
    val type: ChatMessageEventType
)
