package com.rfcoding.core.data.networking

import kotlinx.serialization.Serializable

@Serializable
data class ErrorDto(
    val code: String,
    val message: String
)
