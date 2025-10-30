package com.rfcoding.chat.data.chat.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatDto(
    val id: String,
    val participants: List<ChatParticipantDto>,
    val lastMessage: ChatMessageDto?,
    val isGroupChat: Boolean,
    val name: String?,
    val creator: ChatParticipantDto,
    val lastActivityAt: String,
    val createdAt: String
)
