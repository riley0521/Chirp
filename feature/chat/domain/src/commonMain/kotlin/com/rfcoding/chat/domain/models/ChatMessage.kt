package com.rfcoding.chat.domain.models

import kotlin.time.Instant

data class ChatMessage(
    val id: String,
    val chatId: String,
    val senderId: String?,
    val content: String,
    val messageType: ChatMessageType,
    val imageUrls: List<String>,
    val event: ChatMessageEvent?,
    val createdAt: Instant
) {
    val isTextOnly: Boolean
        get() = content.isNotBlank() && messageType == ChatMessageType.MESSAGE_TEXT

    val isTextWithImages: Boolean
        get() = content.isNotBlank()
                && messageType == ChatMessageType.MESSAGE_TEXT_WITH_IMAGES
                && imageUrls.isNotEmpty()

    // TODO: I don't know if this is supported in the mockups to show a bigger images if content is empty
    val isImagesOnly: Boolean
        get() = content.isBlank()
                && messageType == ChatMessageType.MESSAGE_TEXT_WITH_IMAGES
                && imageUrls.isNotEmpty()

    val isVoiceOverOnly: Boolean
        get() = messageType == ChatMessageType.MESSAGE_VOICE_OVER_ONLY

    val isEvent: Boolean
        get() = event != null && messageType == ChatMessageType.MESSAGE_EVENT
}
