package com.rfcoding.core.data.auth

import com.rfcoding.core.data.auth.dto.RegisterRequest
import com.rfcoding.core.data.networking.post
import com.rfcoding.core.domain.auth.AuthService
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
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
}