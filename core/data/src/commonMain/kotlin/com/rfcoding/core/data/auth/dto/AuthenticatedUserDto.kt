package com.rfcoding.core.data.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticatedUserDto(
    val user: UserDto?,
    val accessToken: String,
    val refreshToken: String,
    val isEmailVerificationTokenSent: Boolean
)
