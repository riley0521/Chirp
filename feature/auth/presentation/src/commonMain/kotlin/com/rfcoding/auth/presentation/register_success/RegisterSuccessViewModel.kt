package com.rfcoding.auth.presentation.register_success

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.core.domain.auth.AuthService
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

class RegisterSuccessViewModel(
    private val authService: AuthService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var hasLoadedInitialData = false

    val registeredEmail = savedStateHandle.get<String>("registeredEmail")
        ?: throw IllegalStateException("No email passed to register success screen")
    private val _state = MutableStateFlow(RegisterSuccessState(registeredEmail = registeredEmail))
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

            when (val result = authService.resendEmailVerification(registeredEmail)) {
                is Result.Failure -> {
                    eventChannel.send(RegisterSuccessEvent.ResendEmailVerificationFailure(
                        error = result.toUiText()
                    ))
                }
                is Result.Success -> {

                }
            }

            _state.update { it.copy(isResendingEmailVerification = false) }
        }
    }
}