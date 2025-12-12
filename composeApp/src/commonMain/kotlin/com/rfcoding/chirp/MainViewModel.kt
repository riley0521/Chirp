package com.rfcoding.chirp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.chat.domain.notification.DeviceTokenService
import com.rfcoding.chat.domain.notification.PushNotificationService
import com.rfcoding.core.data.util.PlatformUtils
import com.rfcoding.core.domain.auth.SessionStorage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val sessionStorage: SessionStorage,
    private val notificationService: PushNotificationService,
    private val deviceTokenService: DeviceTokenService
): ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(MainState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeSession()
                hasLoadedInitialData = true
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000L),
            MainState()
        )

    private val eventChannel = Channel<MainEvent>()
    val events = eventChannel.receiveAsFlow()

    private var previousRefreshToken: String? = null
    private var currentDeviceToken: String? = null
    private var previousDeviceToken: String? = null

    init {
        viewModelScope.launch {
            val authenticatedUser = sessionStorage.observeAuthenticatedUser().firstOrNull()
            _state.update {
                it.copy(
                    isCheckingAuth = false,
                    isLoggedIn = authenticatedUser != null
                )
            }
        }
    }

    private fun observeSession() {
        sessionStorage
            .observeAuthenticatedUser()
            .onEach { authenticatedUser ->
                val currentRefreshToken = authenticatedUser?.refreshToken
                val isSessionExpired = previousRefreshToken != null && currentRefreshToken == null
                if (isSessionExpired) {
                    sessionStorage.set(null)
                    _state.update {
                        it.copy(isLoggedIn = false)
                    }
                    currentDeviceToken?.let {
                        deviceTokenService.unregisterToken(it)
                    }
                    eventChannel.send(MainEvent.OnSessionExpired)
                }

                previousRefreshToken = currentRefreshToken
            }
            .combine(notificationService.observeDeviceToken()) { authenticatedUser, deviceToken ->
                currentDeviceToken = deviceToken
                if (authenticatedUser != null && deviceToken != previousDeviceToken && deviceToken != null) {
                    registerDeviceToken(
                        token = deviceToken,
                        platform = PlatformUtils.getOSName()
                    )
                    previousDeviceToken = deviceToken
                }
            }
            .launchIn(viewModelScope)
    }

    private fun registerDeviceToken(token: String, platform: String) {
        viewModelScope.launch {
            deviceTokenService.registerToken(token, platform)
        }
    }
}