package com.rfcoding.core.data.mappers

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

fun AuthenticatedUser.toSerializable(): AuthenticatedUserDto {
    return AuthenticatedUserDto(
        user = user?.toSerializable(),
        accessToken = accessToken,
        refreshToken = refreshToken,
        isEmailVerificationTokenSent = isEmailVerificationTokenSent
    )
}

fun User.toSerializable(): UserDto {
    return UserDto(
        id = id,
        email = email,
        username = username,
        hasVerifiedEmail = hasVerifiedEmail
    )
}