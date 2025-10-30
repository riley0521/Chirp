package com.rfcoding.chat.domain.models

import kotlin.time.Instant

data class Chat(
    val id: String,
    val participants: Set<ChatParticipant>,
    val lastMessage: ChatMessage?,
    val isGroupChat: Boolean,
    val name: String?,
    val lastActivityAt: Instant,
    val createdAt: Instant
)
