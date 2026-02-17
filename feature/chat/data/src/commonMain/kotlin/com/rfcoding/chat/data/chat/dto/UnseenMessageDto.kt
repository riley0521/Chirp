package com.rfcoding.chat.data.chat.dto

import kotlinx.serialization.Serializable

@Serializable
data class UnseenMessageDto(
    val id: String,
    val createdAt: String
)
