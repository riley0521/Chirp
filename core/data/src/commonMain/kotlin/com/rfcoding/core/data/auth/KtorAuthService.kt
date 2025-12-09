package com.rfcoding.core.data.auth

import com.rfcoding.core.data.auth.dto.AuthenticatedUserDto
import com.rfcoding.core.data.auth.dto.ChangePasswordRequest
import com.rfcoding.core.data.auth.dto.EmailRequest
import com.rfcoding.core.data.auth.dto.LoginRequest
import com.rfcoding.core.data.auth.dto.RefreshTokenRequest
import com.rfcoding.core.data.auth.dto.RegisterRequest
import com.rfcoding.core.data.auth.dto.ResetPasswordRequest
import com.rfcoding.core.data.mappers.toAuthenticatedUser
import com.rfcoding.core.data.networking.get
import com.rfcoding.core.data.networking.post
import com.rfcoding.core.domain.auth.AuthService
import com.rfcoding.core.domain.auth.AuthenticatedUser
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
import com.rfcoding.core.domain.util.Result
import com.rfcoding.core.domain.util.map
import io.ktor.client.HttpClient

class KtorAuthService(
    private val httpClient: HttpClient
): AuthService {

    override suspend fun register(
        email: String,
        username: String,
        password: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = "/auth/register",
            body = RegisterRequest(
                email = email,
                username = username,
                password = password
            )
        )
    }

    override suspend fun resendEmailVerification(email: String): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = "/auth/resend-verification",
            body = EmailRequest(
                email = email
            )
        )
    }

    override suspend fun verifyEmail(token: String): EmptyResult<DataError.Remote> {
        return httpClient.get(
            route = "/auth/verify",
            queryParams = mapOf(
                "token" to token
            )
        )
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthenticatedUser, DataError.Remote> {
        return httpClient.post<LoginRequest, AuthenticatedUserDto>(
            route = "/auth/login",
            body = LoginRequest(
                email = email,
                password = password
            )
        ).map { it.toAuthenticatedUser() }
    }

    override suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote> {
        return httpClient.post<RefreshTokenRequest, Unit>(
            route = "/auth/logout",
            body = RefreshTokenRequest(
                refreshToken = refreshToken
            )
        )
    }

    override suspend fun forgotPassword(email: String): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = "/auth/forgot-password",
            body = EmailRequest(
                email = email
            )
        )
    }

    override suspend fun resetPassword(
        token: String,
        newPassword: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = "/auth/reset-password",
            body = ResetPasswordRequest(
                token = token,
                newPassword = newPassword
            )
        )
    }

    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.post<ChangePasswordRequest, Unit>(
            route = "/auth/change-password",
            body = ChangePasswordRequest(
                oldPassword = oldPassword,
                newPassword = newPassword
            )
        )
    }
}