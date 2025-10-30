package com.rfcoding.chat.data.chat.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateChatRequest(
    val groupChatName: String?,
    val otherUserIds: Set<String>
)
