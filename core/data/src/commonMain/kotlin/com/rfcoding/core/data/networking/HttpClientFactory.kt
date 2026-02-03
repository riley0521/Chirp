package com.rfcoding.core.data.networking

import com.rfcoding.core.data.BuildKonfig
import com.rfcoding.core.data.auth.dto.AuthenticatedUserDto
import com.rfcoding.core.data.auth.dto.RefreshTokenRequest
import com.rfcoding.core.data.mappers.toAuthenticatedUser
import com.rfcoding.core.data.util.currentLocale
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.domain.logging.ChirpLogger
import com.rfcoding.core.domain.util.Result
import com.rfcoding.core.domain.util.map
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json

class HttpClientFactory(
    private val chirpLogger: ChirpLogger,
    private val sessionStorage: SessionStorage
) {

    fun create(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            engine {
                dispatcher = Dispatchers.IO
            }

            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
            install(HttpTimeout) {
                socketTimeoutMillis = 20_000L
                requestTimeoutMillis = 20_000L
            }
            install(Logging) {
                logger = object: Logger {
                    override fun log(message: String) {
                        chirpLogger.debug(message)
                    }
                }
                level = LogLevel.ALL
            }
            install(WebSockets) {
                pingIntervalMillis = 20_000L
            }
            defaultRequest {
                header("x-api-key", BuildKonfig.API_KEY)
                header(HttpHeaders.AcceptLanguage, currentLocale())
                contentType(ContentType.Application.Json)
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        sessionStorage
                            .observeAuthenticatedUser()
                            .firstOrNull()
                            ?.let {
                                BearerTokens(
                                    accessToken = it.accessToken,
                                    refreshToken = it.refreshToken
                                )
                            }
                    }
                    refreshTokens {
                        if (response.request.url.encodedPath.contains("auth/")) {
                            return@refreshTokens null
                        }

                        val authenticatedUser = sessionStorage.observeAuthenticatedUser().firstOrNull()
                        if (authenticatedUser?.refreshToken.isNullOrBlank()) {
                            sessionStorage.set(null)
                            return@refreshTokens null
                        }

                        val result = client.post<RefreshTokenRequest, AuthenticatedUserDto>(
                            route = "/auth/refresh",
                            body = RefreshTokenRequest(
                                refreshToken = authenticatedUser.refreshToken
                            ),
                            builder = {
                                markAsRefreshTokenRequest()
                            }
                        ).map { it.toAuthenticatedUser() }

                        when (result) {
                            is Result.Failure -> {
                                chirpLogger.debug("Refreshing token error message: " + result.message.orEmpty())
                                sessionStorage.set(null)
                                return@refreshTokens null
                            }
                            is Result.Success -> {
                                val authenticatedUser = result.data
                                sessionStorage.set(authenticatedUser)
                                BearerTokens(
                                    accessToken = authenticatedUser.accessToken,
                                    refreshToken = authenticatedUser.refreshToken
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}