package com.rfcoding.chat.database.mapper

import com.rfcoding.chat.database.dao.ChatParticipantDao
import com.rfcoding.chat.database.entities.ChatEntity
import com.rfcoding.chat.database.entities.ChatInfoEntity
import com.rfcoding.chat.database.entities.ChatMediaEntity
import com.rfcoding.chat.database.entities.ChatMessageEntity
import com.rfcoding.chat.database.entities.ChatParticipantEntity
import com.rfcoding.chat.database.entities.ChatWithParticipantsEntity
import com.rfcoding.chat.database.entities.MessageWithSenderEntity
import com.rfcoding.chat.database.entities.UnseenMessageEntity
import com.rfcoding.chat.database.model.ChatMessageEventSerializable
import com.rfcoding.chat.database.model.MediaStatus
import com.rfcoding.chat.database.view.LastMessageView
import com.rfcoding.chat.domain.models.Chat
import com.rfcoding.chat.domain.models.ChatInfo
import com.rfcoding.chat.domain.models.ChatMessage
import com.rfcoding.chat.domain.models.ChatMessageEvent
import com.rfcoding.chat.domain.models.ChatMessageType
import com.rfcoding.chat.domain.models.ChatParticipant
import com.rfcoding.chat.domain.models.Media
import com.rfcoding.chat.domain.models.MediaProgress
import com.rfcoding.chat.domain.models.MediaType
import com.rfcoding.chat.domain.models.MessageWithSender
import com.rfcoding.chat.domain.models.UnseenMessage

fun ChatParticipantEntity.toDomain(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}

fun ChatMessageEventSerializable.toDomain(affectedUsernames: List<String?>): ChatMessageEvent {
    return ChatMessageEvent(
        affectedUsernames = affectedUsernames,
        type = type
    )
}

fun ChatMessageEntity.toDomain(affectedUsernames: List<String?>): ChatMessage {
    return ChatMessage(
        id = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        messageType = chatMessageType,
        imageUrls = imageUrls,
        event = event?.toDomain(affectedUsernames),
        createdAt = createdAt,
        deliveryStatus = deliveryStatus,
        deliveredAt = deliveredAt,
        audioDurationInSeconds = audioDurationInSeconds
    )
}

fun MessageWithSenderEntity.toDomain(affectedUsernames: List<String?>): MessageWithSender {
    val sentMedias = when {
        message.chatMessageType == ChatMessageType.MESSAGE_TEXT_WITH_IMAGES -> {
            message.imageUrls.map {
                Media(
                    name = it,
                    progress = MediaProgress.Sent(it),
                    type = MediaType.IMAGE
                )
            }
        }
        message.chatMessageType == ChatMessageType.MESSAGE_VOICE_OVER_ONLY -> {
            listOf(
                Media(
                    name = message.content,
                    progress = MediaProgress.Sent(message.content),
                    type = MediaType.AUDIO
                )
            )
        }
        else -> emptyList()
    }

    return MessageWithSender(
        message = message.toDomain(affectedUsernames),
        medias = sentMedias + medias.map {
            it.toDomain()
        },
        sender = sender?.toDomain(),
        status = message.deliveryStatus
    )
}

fun ChatMediaEntity.toDomain(): Media {
    return Media(
        name = name,
        progress = when (status) {
            MediaStatus.SENDING -> MediaProgress.Sending(bytes, progress)
            else -> MediaProgress.Failed
        },
        type = type
    )
}

fun LastMessageView.toDomain(affectedUsernames: List<String?>): ChatMessage {
    return ChatMessage(
        id = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        messageType = chatMessageType,
        imageUrls = imageUrls,
        event = event?.toDomain(affectedUsernames),
        createdAt = createdAt,
        deliveryStatus = deliveryStatus,
        deliveredAt = deliveredAt,
        audioDurationInSeconds = audioDurationInSeconds
    )
}

fun ChatWithParticipantsEntity.toDomain(affectedUsernames: List<String?>): Chat {
    return Chat(
        id = chat.chatId,
        participants = participants.map { it?.toDomain() }.toSet(),
        lastMessage = lastMessage?.toDomain(affectedUsernames),
        isGroupChat = chat.isGroupChat,
        name = chat.name,
        creator = creator?.toDomain(),
        lastActivityAt = chat.lastActivityAt,
        unseenMessages = unseenMessages.map { it.toDomain() }
    )
}

fun UnseenMessageEntity.toDomain(): UnseenMessage {
    return UnseenMessage(
        id = messageId,
        createdAt = createdAt
    )
}

fun ChatEntity.toDomain(
    creator: ChatParticipantEntity?,
    participants: Set<ChatParticipantEntity?>
): Chat {
    return Chat(
        id = chatId,
        participants = participants.map { it?.toDomain() }.toSet(),
        lastMessage = null,
        isGroupChat = isGroupChat,
        name = name,
        creator = creator?.toDomain(),
        lastActivityAt = lastActivityAt,
        unseenMessages = emptyList()
    )
}

suspend fun ChatInfoEntity.toDomain(participantDao: ChatParticipantDao): ChatInfo {
    return ChatInfo(
        chat = chat.toDomain(
            creator = creator,
            participants = participants.toSet()
        ),
        messages = messagesWithSenders.map { messageWithSender ->
            val affectedUsernames = participantDao.getUsernamesByUserIds(
                messageWithSender.message.event?.affectedUserIds.orEmpty()
            ).map { it.username }

            messageWithSender.toDomain(affectedUsernames)
        }
    )
}

fun UnseenMessage.toEntity(chatId: String): UnseenMessageEntity {
    return UnseenMessageEntity(
        messageId = id,
        chatId = chatId,
        createdAt = createdAt
    )
}

fun ChatParticipant.toEntity(): ChatParticipantEntity {
    return ChatParticipantEntity(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}

fun Chat.toEntity(): ChatEntity {
    return ChatEntity(
        chatId = id,
        isGroupChat = isGroupChat,
        name = name,
        creatorId = creator?.userId,
        lastActivityAt = lastActivityAt
    )
}



fun ChatMessage.toEntity(affectedUserIds: List<String>): ChatMessageEntity {
    return ChatMessageEntity(
        id = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        chatMessageType = messageType,
        imageUrls = imageUrls,
        event = event?.toSerializable(affectedUserIds),
        createdAt = createdAt,
        deliveryStatus = deliveryStatus,
        deliveredAt = deliveredAt,
        audioDurationInSeconds = audioDurationInSeconds
    )
}

fun ChatMessage.toDatabaseView(affectedUserIds: List<String>): LastMessageView {
    return LastMessageView(
        id = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        chatMessageType = messageType,
        imageUrls = imageUrls,
        event = event?.toSerializable(affectedUserIds),
        createdAt = createdAt,
        deliveryStatus = deliveryStatus,
        deliveredAt = deliveredAt,
        audioDurationInSeconds = audioDurationInSeconds
    )
}

fun ChatMessageEvent.toSerializable(affectedUserIds: List<String>): ChatMessageEventSerializable {
    return ChatMessageEventSerializable(
        affectedUserIds = affectedUserIds,
        type = type
    )
}