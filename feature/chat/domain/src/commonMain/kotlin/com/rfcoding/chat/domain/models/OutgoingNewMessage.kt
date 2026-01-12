package com.rfcoding.chat.domain.models

data class OutgoingNewMessage(
    val messageId: String,
    val chatId: String,
    val content: String
)
