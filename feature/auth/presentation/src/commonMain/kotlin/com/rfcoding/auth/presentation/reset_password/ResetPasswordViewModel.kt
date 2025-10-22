package com.rfcoding.auth.presentation.reset_password

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.core.domain.validation.PasswordValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ResetPasswordViewModel(
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

    private val isPasswordValidFlow = snapshotFlow { state.value.passwordTextFieldState.text.toString() }
        .map { password -> PasswordValidator.validate(password).isValidPassword }
        .distinctUntilChanged()

    private val isLoadingFlow = state
        .map { it.isLoading }
        .distinctUntilChanged()

    private val token = savedStateHandle.get<String>("token") ?: "Invalid"

    private fun observeFormInputs() {
        combine(
            isPasswordValidFlow,
            isLoadingFlow
        ) { isPasswordValid, isLoading ->
            _state.update {
                it.copy(canSubmit = isPasswordValid && !isLoading)
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: ResetPasswordAction) {
        when (action) {
            ResetPasswordAction.OnSubmitClick -> {}
            ResetPasswordAction.OnTogglePasswordVisibilityClick -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
        }
    }
}