package com.rfcoding.chat.data.chat.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatParticipantDto(
    val userId: String,
    val username: String,
    val email: String,
    val profilePictureUrl: String?
)
