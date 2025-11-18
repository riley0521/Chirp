package com.rfcoding.chat.database.mapper

import com.rfcoding.chat.database.dao.ChatDao
import com.rfcoding.chat.database.dao.ChatParticipantDao
import com.rfcoding.chat.database.entities.ChatEntity
import com.rfcoding.chat.database.entities.ChatInfoEntity
import com.rfcoding.chat.database.entities.ChatMessageEntity
import com.rfcoding.chat.database.entities.ChatParticipantEntity
import com.rfcoding.chat.database.entities.ChatWithParticipantsEntity
import com.rfcoding.chat.database.entities.MessageWithSenderEntity
import com.rfcoding.chat.database.model.ChatMessageEventSerializable
import com.rfcoding.chat.database.view.LastMessageView
import com.rfcoding.chat.domain.models.Chat
import com.rfcoding.chat.domain.models.ChatInfo
import com.rfcoding.chat.domain.models.ChatMessage
import com.rfcoding.chat.domain.models.ChatMessageEvent
import com.rfcoding.chat.domain.models.ChatParticipant
import com.rfcoding.chat.domain.models.MessageWithSender
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.mapNotNull

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
        deliveryStatus = deliveryStatus
    )
}

fun MessageWithSenderEntity.toDomain(affectedUsernames: List<String?>): MessageWithSender {
    return MessageWithSender(
        message = message.toDomain(affectedUsernames),
        sender = sender?.toDomain(),
        status = message.deliveryStatus
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
        deliveryStatus = deliveryStatus
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
        lastActivityAt = lastMessage?.createdAt ?: chat.lastActivityAt
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
        lastActivityAt = lastActivityAt
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
        deliveryStatus = deliveryStatus
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
        deliveryStatus = deliveryStatus
    )
}

fun ChatMessageEvent.toSerializable(affectedUserIds: List<String>): ChatMessageEventSerializable {
    return ChatMessageEventSerializable(
        affectedUserIds = affectedUserIds,
        type = type
    )
}

suspend fun sampleF(chatDao: ChatDao, chatParticipantDao: ChatParticipantDao) = coroutineScope {
    chatDao
        .getChatInfoById("1")
        .mapNotNull { chatInfo ->
            val mapped = chatInfo?.messagesWithSenders?.map { messageWithSender ->
                async {
                    val affectedUsernames = messageWithSender.message.event?.let { event ->
                        chatParticipantDao.getUsernamesByUserIds(event.affectedUserIds).map { it.username }
                    }.orEmpty()

                    MessageWithSender(
                        message = messageWithSender.message.toDomain(affectedUsernames),
                        sender = messageWithSender.sender?.toDomain(),
                        status = messageWithSender.message.deliveryStatus
                    )
                }
            }

            ChatInfo(
                chat = chatInfo?.chat?.toDomain(
                    creator = chatInfo.creator,
                    participants = chatInfo.participants.toSet()
                ) ?: return@mapNotNull null,
                messages = mapped?.awaitAll().orEmpty()
            )
        }
}