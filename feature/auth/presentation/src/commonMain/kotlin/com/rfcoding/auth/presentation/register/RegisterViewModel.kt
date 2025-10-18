package com.rfcoding.auth.presentation.register

import androidx.compose.foundation.text.input.toTextFieldBuffer
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chirp.feature.auth.presentation.generated.resources.Res
import chirp.feature.auth.presentation.generated.resources.error_invalid_email
import chirp.feature.auth.presentation.generated.resources.error_invalid_password
import chirp.feature.auth.presentation.generated.resources.error_invalid_username
import com.rfcoding.auth.domain.EmailValidator
import com.rfcoding.core.domain.validation.PasswordValidator
import com.rfcoding.core.presentation.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class RegisterViewModel : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(RegisterState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                /** Load initial data here **/
                hasLoadedInitialData = true
                observeFormInputs()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = RegisterState()
        )

    private val usernameFlow = snapshotFlow { state.value.usernameTextState.text.toString() }
    private val emailFlow = snapshotFlow { state.value.emailTextState.text.toString() }
    private val passwordFlow = snapshotFlow { state.value.passwordTextState.text.toString() }

    fun observeFormInputs() {
        combine(
            usernameFlow,
            emailFlow,
            passwordFlow
        ) { username, email, password ->
            _state.update {
                it.copy(
                    canRegister = username.isNotBlank() && email.isNotBlank() && password.isNotBlank()
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: RegisterAction) {
        when (action) {
            RegisterAction.OnLoginClick -> {}
            RegisterAction.OnRegisterClick -> {
                validateFormInputs()
            }
            RegisterAction.OnTogglePasswordVisibilityClick -> {
                _state.update { it.copy(isPasswordVisible = !state.value.isPasswordVisible) }
            }
        }
    }

    private fun clearAllTextFieldErrors() {
        _state.update {
            it.copy(
                usernameError = null,
                emailError = null,
                passwordError = null,
                registrationError = null
            )
        }
    }

    private fun validateFormInputs(): Boolean {
        clearAllTextFieldErrors()

        val currentState = state.value
        val username = currentState.usernameTextState.text.toString()
        val email = currentState.emailTextState.text.toString()
        val password = currentState.passwordTextState.text.toString()

        val isUsernameValid = username.isNotBlank() && username.length in 3..20
        val isEmailValid = EmailValidator.validate(email)
        val passwordValidationState = PasswordValidator.validate(password)

        val usernameError = if (!isUsernameValid) {
            UiText.Resource(Res.string.error_invalid_username)
        } else null
        val emailError = if (!isEmailValid) {
            UiText.Resource(Res.string.error_invalid_email)
        } else null
        val passwordError = if (!passwordValidationState.isValidPassword) {
            UiText.Resource(Res.string.error_invalid_password)
        } else null

        _state.update {
            it.copy(
                usernameError = usernameError,
                emailError = emailError,
                passwordError = passwordError
            )
        }

        return isUsernameValid && isEmailValid && passwordValidationState.isValidPassword
    }
}