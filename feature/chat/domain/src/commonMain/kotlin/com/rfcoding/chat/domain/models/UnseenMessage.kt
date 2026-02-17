package com.rfcoding.chat.domain.models

import kotlin.time.Instant

data class UnseenMessage(
    val id: String,
    val createdAt: Instant
)
