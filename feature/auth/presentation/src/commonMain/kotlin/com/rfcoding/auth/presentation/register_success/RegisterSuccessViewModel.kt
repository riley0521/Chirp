package com.rfcoding.auth.presentation.register_success

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterSuccessViewModel : ViewModel() {

    private var hasLoadedInitialData = false

    // TODO: Fix registeredEmail received from navigation.
    private val _state = MutableStateFlow(RegisterSuccessState(registeredEmail = "hello@chirp.com"))
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                /** Load initial data here **/
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = RegisterSuccessState()
        )

    private val eventChannel = Channel<RegisterSuccessEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: RegisterSuccessAction) {
        when (action) {
            RegisterSuccessAction.OnLoginClick -> onLoginClick()
            RegisterSuccessAction.OnResendEmailVerificationClick -> resendEmailVerification()
        }
    }

    private fun onLoginClick() {
        viewModelScope.launch {
            eventChannel.send(RegisterSuccessEvent.Login)
        }
    }

    private fun resendEmailVerification() {
        viewModelScope.launch {
            _state.update { it.copy(isResendingEmailVerification = true) }

            delay(3_000L)

            _state.update { it.copy(isResendingEmailVerification = false) }
        }
    }
}