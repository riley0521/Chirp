package com.rfcoding.chat.domain.models

data class ChatParticipant(
    val userId: String,
    val username: String,
    val email: String,
    val profilePictureUrl: String?
) {
    val initials: String
        get() = username.take(2).uppercase()
}
