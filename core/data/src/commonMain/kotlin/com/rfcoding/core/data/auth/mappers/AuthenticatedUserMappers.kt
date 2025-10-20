package com.rfcoding.core.data.auth.mappers

import com.rfcoding.core.data.auth.dto.AuthenticatedUserDto
import com.rfcoding.core.data.auth.dto.UserDto
import com.rfcoding.core.domain.auth.AuthenticatedUser
import com.rfcoding.core.domain.auth.User

fun AuthenticatedUserDto.toAuthenticatedUser(): AuthenticatedUser {
    return AuthenticatedUser(
        user = user?.toUser(),
        accessToken = accessToken,
        refreshToken = refreshToken,
        isEmailVerificationTokenSent = isEmailVerificationTokenSent
    )
}

fun UserDto.toUser(): User {
    return User(
        id = id,
        email = email,
        username = username,
        hasVerifiedEmail = hasVerifiedEmail
    )
}