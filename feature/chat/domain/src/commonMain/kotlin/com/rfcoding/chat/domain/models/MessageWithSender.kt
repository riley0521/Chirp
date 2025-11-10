package com.rfcoding.chat.domain.models

data class MessageWithSender(
    val message: ChatMessage,
    val sender: ChatParticipant?,
    val status: ChatMessageDeliveryStatus?,
    val affectedUsernames: List<String?>
)
