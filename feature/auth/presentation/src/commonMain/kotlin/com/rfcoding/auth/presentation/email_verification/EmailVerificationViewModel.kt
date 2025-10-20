package com.rfcoding.auth.presentation.email_verification

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.core.domain.auth.AuthService
import com.rfcoding.core.domain.logging.ChirpLogger
import com.rfcoding.core.domain.util.Result
import com.rfcoding.core.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EmailVerificationViewModel(
    private val authService: AuthService,
    private val chirpLogger: ChirpLogger,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(EmailVerificationState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                verifyEmail()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = EmailVerificationState()
        )

    private val eventChannel = Channel<EmailVerificationEvent>()
    val events = eventChannel.receiveAsFlow()

    private val token = savedStateHandle.get<String>("token")

    fun onAction(action: EmailVerificationAction) {
        when (action) {
            EmailVerificationAction.OnLoginClick,
            EmailVerificationAction.OnCloseClick -> loginOrClose()
        }
    }

    private fun verifyEmail() {
        viewModelScope.launch {
            _state.update { it.copy(isVerifying = true) }

            when (val result = authService.verifyEmail(token ?: "Invalid")) {
                is Result.Failure -> {
                    chirpLogger.debug(result.toUiText().asStringAsync())
                }
                is Result.Success -> {
                    _state.update { it.copy(isVerified = true) }
                }
            }

            _state.update { it.copy(isVerifying = false) }
        }
    }

    private fun loginOrClose() {
        viewModelScope.launch {
            eventChannel.send(EmailVerificationEvent.Login)
        }
    }

}