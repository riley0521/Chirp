package com.rfcoding.chat.database.mapper

import com.rfcoding.chat.database.dao.ChatDao
import com.rfcoding.chat.database.dao.ChatParticipantDao
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

fun ChatMessageEventSerializable.toDomain(): ChatMessageEvent {
    return ChatMessageEvent(
        affectedUserIds = affectedUserIds,
        type = type
    )
}

fun ChatMessageEntity.toDomain(): ChatMessage {
    return ChatMessage(
        id = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        messageType = chatMessageType,
        imageUrls = imageUrls,
        event = event?.toDomain(),
        createdAt = createdAt
    )
}

fun MessageWithSenderEntity.toDomain(affectedUsernames: List<String>): MessageWithSender {
    return MessageWithSender(
        message = message.toDomain(),
        sender = sender?.toDomain(),
        status = message.deliveryStatus,
        affectedUsernames = affectedUsernames
    )
}

fun LastMessageView.toDomain(): ChatMessage {
    return ChatMessage(
        id = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        messageType = chatMessageType,
        imageUrls = imageUrls,
        event = event?.toDomain(),
        createdAt = createdAt
    )
}

fun ChatWithParticipantsEntity.toDomain(): Chat {
    return Chat(
        id = chat.chatId,
        participants = participants.map { it?.toDomain() }.toSet(),
        lastMessage = lastMessage?.toDomain(),
        isGroupChat = chat.isGroupChat,
        name = chat.name,
        creator = null,
        lastActivityAt = lastMessage?.toDomain()?.createdAt ?: chat.lastActivityAt
    )
}

fun ChatInfoEntity.toDomain(): Chat {
    return Chat(
        id = chat.chatId,
        participants = participants.map { it?.toDomain() }.toSet(),
        lastMessage = null,
        isGroupChat = chat.isGroupChat,
        name = chat.name,
        creator = creator?.toDomain(),
        lastActivityAt = chat.lastActivityAt
    )
}

suspend fun sampleF(chatDao: ChatDao, chatParticipantDao: ChatParticipantDao) = coroutineScope {
    chatDao
        .getChatInfoById("1")
        .mapNotNull { chatInfo ->
            val mapped = chatInfo?.messagesWithSenders?.map { messageWithSender ->
                async {
                    val affectedUsernames = messageWithSender.message.event?.let {
                        chatParticipantDao.getUsernamesByUserIds(it.affectedUserIds)
                    }.orEmpty()

                    MessageWithSender(
                        message = messageWithSender.message.toDomain(),
                        sender = messageWithSender.sender?.toDomain(),
                        status = messageWithSender.message.deliveryStatus,
                        affectedUsernames = affectedUsernames
                    )
                }
            }

            ChatInfo(
                chat = chatInfo?.toDomain() ?: return@mapNotNull null,
                messages = mapped?.awaitAll().orEmpty()
            )
        }
}