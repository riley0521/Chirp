package com.rfcoding.chat.data.notification.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceRequest(
    val token: String,
    val platform: String
)