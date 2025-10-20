package com.rfcoding.auth.presentation.login

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.auth.domain.EmailValidator
import com.rfcoding.core.domain.validation.PasswordValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel: ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(LoginState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeInputFields()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = LoginState()
        )

    private val eventChannel = Channel<LoginEvent>()
    val events = eventChannel.receiveAsFlow()

    private val isEmailValidFlow = snapshotFlow { state.value.emailTextFieldState.text.toString() }
        .map { email -> EmailValidator.validate(email) }
    private val isPasswordValidFlow = snapshotFlow { state.value.emailTextFieldState.text.toString() }
        .map { password -> PasswordValidator.validate(password).isValidPassword }
    private val isLoggingInFlow = state.map { it.isLoggingIn }

    private fun observeInputFields() {
        combine(
            isEmailValidFlow,
            isPasswordValidFlow,
            isLoggingInFlow
        ) { isEmailValid, isPasswordValid, isLoggingIn ->
            _state.update {
                it.copy(
                    canLogin = !isLoggingIn && isEmailValid && isPasswordValid
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.OnForgotPasswordClick -> forgotPassword()
            LoginAction.OnLoginClick -> login()
            LoginAction.OnRegisterClick -> register()
            LoginAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
        }
    }

    private fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoggingIn = true) }

            delay(3_000L)

            _state.update { it.copy(isLoggingIn = false) }
        }
    }

    private fun register() {
        viewModelScope.launch {
            eventChannel.send(LoginEvent.Register)
        }
    }

    private fun forgotPassword() {
        viewModelScope.launch {
            eventChannel.send(LoginEvent.ForgotPassword)
        }
    }

}