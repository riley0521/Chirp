package com.rfcoding.chat.data.network

import com.rfcoding.chat.data.chat.dto.websocket.WebSocketMessageDto
import com.rfcoding.chat.data.lifecycle.AppLifecycleObserver
import com.rfcoding.chat.domain.models.ConnectionState
import com.rfcoding.core.data.util.currentLocale
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.domain.logging.ChirpLogger
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
import com.rfcoding.core.domain.util.Result
import com.rfcoding.feature.chat.data.BuildKonfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
class KtorWebSocketConnector(
    private val httpClient: HttpClient,
    private val applicationScope: CoroutineScope,
    private val sessionStorage: SessionStorage,
    private val json: Json,
    private val errorHandler: ConnectionErrorHandler,
    private val retryHandler: ConnectionRetryHandler,
    private val appLifecycleObserver: AppLifecycleObserver,
    private val connectivityObserver: ConnectivityObserver,
    private val logger: ChirpLogger
) {

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private var currentSession: WebSocketSession? = null

    private val isConnected = connectivityObserver
        .isConnected
        .debounce(1.seconds)
        .stateIn(
            applicationScope,
            SharingStarted.WhileSubscribed(5_000L),
            false
        )

    private val isInForeground = appLifecycleObserver
        .isInForeground
        .onEach { isInForeground ->
            if (isInForeground) {
                retryHandler.resetDelay()
            }
        }
        .stateIn(
            applicationScope,
            SharingStarted.WhileSubscribed(5_000L),
            true
        )

    val messages = combine(
        sessionStorage.observeAuthenticatedUser(),
        isConnected,
        isInForeground
    ) { authInfo, isConnected, isInForeground ->
        when {
            authInfo == null -> {
                logger.info("No authentication details. Clearing session and disconnecting...")
                resetState()
                retryHandler.resetDelay()
                null
            }

            !isInForeground -> {
                logger.info("App in background, disconnecting...")
                resetState()
                retryHandler.resetDelay()
                null
            }

            !isConnected -> {
                logger.info("Device is disconnected from Internet")
                _connectionState.value = ConnectionState.ERROR_NETWORK
                currentSession?.close()
                currentSession = null
                null
            }

            else -> {
                logger.info("Establishing connection...")

                if (_connectionState.value !in listOf(
                        ConnectionState.CONNECTING,
                        ConnectionState.CONNECTED
                    )
                ) {
                    _connectionState.value = ConnectionState.CONNECTING
                }

                authInfo
            }
        }
    }.filterNotNull().flatMapLatest { authInfo ->
        createWebSocketFlow(authInfo.accessToken)
            .catch { e ->
                logger.error("Exception in WebSocket", e)

                currentSession?.close()
                currentSession = null

                throw errorHandler.transformException(e)
            }
            .retryWhen { t, attempt ->
                logger.info("Connection failed on attempt $attempt")

                val shouldRetry = retryHandler.shouldRetry(t)
                if (shouldRetry) {
                    _connectionState.value = ConnectionState.CONNECTING
                    retryHandler.applyRetryDelay(attempt)
                }

                shouldRetry
            }
            .catch { e ->
                logger.error("Unhandled error", e)
                _connectionState.value = errorHandler.getConnectionStateForError(e)
            }
    }

    private suspend fun resetState() {
        _connectionState.value = ConnectionState.DISCONNECTED
        currentSession?.close()
        currentSession = null
    }

    private fun createWebSocketFlow(accessToken: String) = callbackFlow {
        _connectionState.value = ConnectionState.CONNECTING

        currentSession = httpClient.webSocketSession(
            urlString = "${BuildKonfig.BASE_URL_WS}/chats"
        ) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            header(HttpHeaders.AcceptLanguage, currentLocale())
        }

        currentSession?.let { session ->
            _connectionState.value = ConnectionState.CONNECTED

            session
                .incoming
                .consumeAsFlow()
                .buffer(
                    capacity = 100
                )
                .collect { frame ->
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            logger.info("Received raw text frame: $text")

                            val messageDto = json.decodeFromString<WebSocketMessageDto>(text)
                            send(messageDto)
                        }

                        is Frame.Ping -> {
                            logger.debug("Received ping from server. Sending pong...")
                            session.send(Frame.Pong(frame.data))
                        }

                        else -> Unit
                    }
                }
        } ?: throw Exception("Failed to establish web socket connection.")

        awaitClose {
            launch(NonCancellable) {
                logger.info("Disconnecting from WebSocket session...")
                resetState()
            }
        }
    }

    suspend fun sendMessage(message: String): EmptyResult<DataError.Connection> {
        val connectionState = _connectionState.value

        if (currentSession == null || connectionState != ConnectionState.CONNECTED) {
            return Result.Failure(DataError.Connection.NOT_CONNECTED)
        }

        return try {
            currentSession?.send(message)
            Result.Success(Unit)
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            logger.error("Unable to send message: $message", e)

            Result.Failure(DataError.Connection.MESSAGE_SEND_FAILED)
        }
    }
}