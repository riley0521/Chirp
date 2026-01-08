package com.rfcoding.chat.domain.models

data class OutgoingNewMessage(
    val messageId: String,
    val chatId: String,
    val content: String,
    val uploadedImageUrls: List<String> = emptyList(),
    val audioDurationInSeconds: Int = 0
)
