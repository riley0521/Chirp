package com.rfcoding.auth.presentation.login

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chirp.feature.auth.presentation.generated.resources.Res
import chirp.feature.auth.presentation.generated.resources.verification_email_sent_to_x
import com.rfcoding.auth.domain.EmailValidator
import com.rfcoding.core.domain.auth.AuthService
import com.rfcoding.core.domain.auth.AuthenticatedUser
import com.rfcoding.core.domain.logging.ChirpLogger
import com.rfcoding.core.domain.util.Result
import com.rfcoding.core.presentation.util.UiText
import com.rfcoding.core.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authService: AuthService,
    private val chirpLogger: ChirpLogger
): ViewModel() {

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
        .distinctUntilChanged()
    private val isPasswordValidFlow = snapshotFlow { state.value.passwordTextFieldState.text.toString() }
        .map { password -> password.isNotBlank() }
        .distinctUntilChanged()
    private val isLoggingInFlow = state
        .map { it.isLoggingIn }
        .distinctUntilChanged()

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
        if (state.value.isLoggingIn) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoggingIn = true, error = null) }

            val email = state.value.emailTextFieldState.text.toString()
            val password = state.value.passwordTextFieldState.text.toString()
            when (val result = authService.login(email, password)) {
                is Result.Failure -> {
                    _state.update { it.copy(error = result.toUiText()) }
                }
                is Result.Success -> {
                    chirpLogger.debug("""
                        Access token: ${result.data.accessToken}
                        Refresh token: ${result.data.refreshToken}
                    """.trimIndent())
                    when {
                        result.data.user != null -> {
                            cacheAuthenticatedUser(result.data)
                            eventChannel.send(LoginEvent.LoginSuccessful)
                        }
                        result.data.isEmailVerificationTokenSent -> {
                            _state.update {
                                it.copy(
                                    error = UiText.Resource(Res.string.verification_email_sent_to_x, arrayOf(email))
                                )
                            }
                        }
                        // If the user is null AND the isEmailVerificationTokenSent == false
                        // It means that the user is unverified and hit the rate limit for sending emails, it will go to Failure automatically.
                        else -> Unit
                    }
                }
            }

            _state.update { it.copy(isLoggingIn = false) }
        }
    }

    private fun cacheAuthenticatedUser(data: AuthenticatedUser) {
        // TODO: Cache tokens to shared preference.
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