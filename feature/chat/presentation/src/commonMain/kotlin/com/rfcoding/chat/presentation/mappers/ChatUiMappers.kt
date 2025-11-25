package com.rfcoding.chat.presentation.mappers

import com.rfcoding.chat.domain.models.Chat
import com.rfcoding.chat.domain.models.ChatParticipant
import com.rfcoding.chat.domain.models.MessageWithSender
import com.rfcoding.chat.presentation.model.ChatUi
import com.rfcoding.chat.presentation.model.MessageUi
import com.rfcoding.chat.presentation.util.DateUtils
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi

/**
 * @param localUserId Id of the user currently logged in.
 * @param lastMessageUsername can be a user that was removed from the chat long ago and
 * cannot be fetched from active participants.
 * @param affectedUsernamesForEvent In ${lastMessage.event} there is a field affectedUserIds,
 * we don't want that. What we want is their username.
 */
fun Chat.toUi(
    localUserId: String,
    lastMessageUsername: String?,
    affectedUsernamesForEvent: List<String?>
): ChatUi {
    val (local, other) = participants.partition { it?.userId == localUserId }
    return ChatUi(
        id = id,
        localParticipant = local.first()!!.toUi(),
        participants = other.map { it?.toUi() },
        lastMessage = lastMessage,
        lastMessageUsername = lastMessageUsername,
        affectedUsernamesForEvent = affectedUsernamesForEvent,
        isGroupChat = isGroupChat,
        creatorId = creator?.userId,
        name = name
    )
}

fun MessageWithSender.toUi(
    localUserId: String,
    isMenuOpen: Boolean
): MessageUi {
    val isFromLocalUser = sender?.userId == localUserId
    val isEvent = message.isEvent

    return with (message) {
        when {
            isFromLocalUser -> MessageUi.LocalUserMessage(
                id = id,
                content = content,
                deliveryStatus = deliveryStatus,
                isMenuOpen = isMenuOpen,
                formattedSentTime = DateUtils.formatMessageTime(deliveredAt),
                imageUrls = imageUrls,
                messageType = messageType
            )
            isEvent -> {
                MessageUi.EventMessage(
                    id = id,
                    type = event!!.type,
                    username = sender?.username,
                    affectedUsernames = event!!.affectedUsernames
                )
            }
            else -> MessageUi.OtherUserMessage(
                id = id,
                content = content,
                sender = sender?.toUi(),
                formattedSentTime = DateUtils.formatMessageTime(createdAt),
                imageUrls = imageUrls,
                messageType = messageType
            )
        }
    }
}

fun ChatParticipant.toUi(): ChatParticipantUi {
    return ChatParticipantUi(
        id = userId,
        username = username,
        initial = initials,
        imageUrl = profilePictureUrl
    )
}