package com.rfcoding.auth.presentation.reset_password

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.core.domain.auth.AuthService
import com.rfcoding.core.domain.util.Result
import com.rfcoding.core.domain.validation.PasswordValidator
import com.rfcoding.core.presentation.util.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val authService: AuthService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(ResetPasswordState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeFormInputs()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ResetPasswordState()
        )

    private val token = savedStateHandle.get<String>("token") ?: "Invalid"

    private val passwordFlow = snapshotFlow { state.value.passwordTextFieldState.text.toString() }

    private fun observeFormInputs() {
        passwordFlow
            .onEach { password ->
                val canSubmit = PasswordValidator.validate(password).isValidPassword
                _state.update {
                    it.copy(canSubmit = canSubmit)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: ResetPasswordAction) {
        when (action) {
            ResetPasswordAction.OnSubmitClick -> submit()
            ResetPasswordAction.OnTogglePasswordVisibilityClick -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
        }
    }

    private fun submit() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val newPassword = state.value.passwordTextFieldState.text.toString()
            when (val result = authService.resetPassword(token, newPassword)) {
                is Result.Failure -> {
                    _state.update { it.copy(error = result.toUiText()) }
                }
                is Result.Success -> {
                    _state.update { it.copy(isResetPasswordSuccessful = true) }
                }
            }

            _state.update { it.copy(isLoading = false) }
        }
    }
}