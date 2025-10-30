package com.rfcoding.chat.data.mappers

import com.rfcoding.chat.data.chat.dto.ChatParticipantDto
import com.rfcoding.chat.domain.models.ChatParticipant

fun ChatParticipantDto.toDomain(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}