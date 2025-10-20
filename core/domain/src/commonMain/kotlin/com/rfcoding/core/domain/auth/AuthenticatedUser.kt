package com.rfcoding.core.domain.auth

data class AuthenticatedUser(
    val user: User?,
    val accessToken: String,
    val refreshToken: String,
    val isEmailVerificationTokenSent: Boolean
)
