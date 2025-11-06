package com.rfcoding.chat.database.model

import com.rfcoding.chat.domain.models.ChatMessageEventType
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageEventSerializable(
    val affectedUserIds: List<String>,
    val type: ChatMessageEventType
)
