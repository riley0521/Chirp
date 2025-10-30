package com.rfcoding.chat.presentation.mappers

import com.rfcoding.chat.domain.models.ChatParticipant
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi

fun ChatParticipant.toUi(): ChatParticipantUi {
    return ChatParticipantUi(
        id = userId,
        username = username,
        initial = initials,
        imageUrl = profilePictureUrl
    )
}