package com.rfcoding.chat.domain.models

data class MessageWithSender(
    val message: ChatMessage,
    val medias: List<Media>,
    val sender: ChatParticipant?,
    val status: ChatMessageDeliveryStatus?
)
