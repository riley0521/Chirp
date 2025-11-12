package com.rfcoding.chat.data.mappers

import com.rfcoding.chat.data.chat.dto.ChatDto
import com.rfcoding.chat.data.chat.dto.ChatMessageDto
import com.rfcoding.chat.data.chat.dto.ChatMessageEventDto
import com.rfcoding.chat.data.chat.dto.ChatParticipantDto
import com.rfcoding.chat.domain.models.Chat
import com.rfcoding.chat.domain.models.ChatMessage
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.domain.models.ChatMessageEvent
import com.rfcoding.chat.domain.models.ChatParticipant
import kotlin.time.Instant

fun ChatParticipantDto.toDomain(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}

fun ChatMessageDto.toDomain(): ChatMessage {
    return ChatMessage(
        id = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        messageType = messageType,
        imageUrls = imageUrls,
        event = event?.toDomain(),
        createdAt = Instant.parse(createdAt),
        deliveryStatus = ChatMessageDeliveryStatus.SENT
    )
}

fun ChatMessageEventDto.toDomain(): ChatMessageEvent {
    return ChatMessageEvent(
        affectedUsernames = emptyList(),
        type = type
    )
}

fun ChatDto.toDomain(): Chat {
    return Chat(
        id = id,
        participants = participants.map { it.toDomain() }.toSet(),
        lastMessage = lastMessage?.toDomain(),
        isGroupChat = isGroupChat,
        name = name,
        creator = creator?.toDomain(),
        lastActivityAt = Instant.parse(lastActivityAt)
    )
}