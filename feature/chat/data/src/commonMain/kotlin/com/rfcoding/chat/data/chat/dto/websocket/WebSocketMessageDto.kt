package com.rfcoding.chat.data.chat.dto.websocket

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketMessageDto(
    val type: String,
    val payload: String
)
