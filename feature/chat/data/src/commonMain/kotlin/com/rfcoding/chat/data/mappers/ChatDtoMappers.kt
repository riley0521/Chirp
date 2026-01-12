package com.rfcoding.chat.data.mappers

import com.rfcoding.chat.data.chat.dto.ChatDto
import com.rfcoding.chat.data.chat.dto.ChatMessageDto
import com.rfcoding.chat.data.chat.dto.ChatMessageEventDto
import com.rfcoding.chat.data.chat.dto.ChatParticipantDto
import com.rfcoding.chat.data.chat.dto.websocket.IncomingWebSocketDto
import com.rfcoding.chat.data.chat.dto.websocket.IncomingWebSocketType
import com.rfcoding.chat.data.chat.dto.websocket.WebSocketMessageDto
import com.rfcoding.chat.domain.models.Chat
import com.rfcoding.chat.domain.models.ChatMessage
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.domain.models.ChatMessageEvent
import com.rfcoding.chat.domain.models.ChatParticipant
import kotlinx.serialization.json.Json
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
        deliveryStatus = ChatMessageDeliveryStatus.SENT,
        audioDurationInSeconds = audioDurationInSeconds
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

fun WebSocketMessageDto.toIncomingWebSocketDto(json: Json): IncomingWebSocketDto? {
    val webSocketType = try {
        IncomingWebSocketType.valueOf(type)
    } catch (_: Exception) {
        null
    }
    return when (webSocketType) {
        IncomingWebSocketType.NEW_MESSAGE -> {
            json.decodeFromString<IncomingWebSocketDto.NewMessage>(payload)
        }
        IncomingWebSocketType.MESSAGE_DELETED -> {
            json.decodeFromString<IncomingWebSocketDto.DeleteMessage>(payload)
        }
        IncomingWebSocketType.PROFILE_PICTURE_UPDATED -> {
            json.decodeFromString<IncomingWebSocketDto.ProfilePictureUpdated>(payload)
        }
        IncomingWebSocketType.USER_TYPING -> {
            json.decodeFromString<IncomingWebSocketDto.UserTyping>(payload)
        }
        else -> null
    }
}

suspend fun IncomingWebSocketDto.NewMessage.toDomain(
    fetchUsernames: suspend (List<String>) -> List<String?>,
    deliveryStatus: ChatMessageDeliveryStatus = ChatMessageDeliveryStatus.SENT
): ChatMessage {
    return ChatMessage(
        id = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        messageType = messageType,
        imageUrls = imageUrls,
        event = event?.let {
            ChatMessageEvent(
                affectedUsernames = fetchUsernames(it.affectedUserIds),
                type = it.type
            )
        },
        createdAt = Instant.parse(createdAt),
        deliveryStatus = deliveryStatus,
        audioDurationInSeconds = audioDurationInSeconds
    )
}