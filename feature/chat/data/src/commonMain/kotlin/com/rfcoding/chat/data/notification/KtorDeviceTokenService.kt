package com.rfcoding.chat.data.notification

import com.rfcoding.chat.data.notification.dto.RegisterDeviceRequest
import com.rfcoding.chat.domain.notification.DeviceTokenService
import com.rfcoding.core.data.networking.delete
import com.rfcoding.core.data.networking.post
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
import io.ktor.client.HttpClient

class KtorDeviceTokenService(
    private val httpClient: HttpClient
): DeviceTokenService {

    override suspend fun registerToken(
        token: String,
        platform: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.post<RegisterDeviceRequest, Unit>(
            route = "/notifications",
            body = RegisterDeviceRequest(
                token = token,
                platform = platform
            )
        )
    }

    override suspend fun unregisterToken(token: String): EmptyResult<DataError.Remote> {
        return httpClient.delete(
            "/notifications/$token"
        )
    }
}