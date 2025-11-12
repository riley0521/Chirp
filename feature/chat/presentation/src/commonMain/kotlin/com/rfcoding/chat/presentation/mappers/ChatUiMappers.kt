package com.rfcoding.chat.presentation.mappers

import com.rfcoding.chat.domain.models.Chat
import com.rfcoding.chat.domain.models.ChatParticipant
import com.rfcoding.chat.presentation.model.ChatUi
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

fun ChatParticipant.toUi(): ChatParticipantUi {
    return ChatParticipantUi(
        id = userId,
        username = username,
        initial = initials,
        imageUrl = profilePictureUrl
    )
}