package com.rfcoding.auth.presentation.forgot_password

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.auth.domain.EmailValidator
import com.rfcoding.core.domain.auth.AuthService
import com.rfcoding.core.domain.util.Result
import com.rfcoding.core.presentation.util.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authService: AuthService
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(ForgotPasswordState())
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
            initialValue = ForgotPasswordState()
        )

    private val isEmailValidFlow = snapshotFlow { state.value.emailTextFieldState.text.toString() }
        .map { email -> EmailValidator.validate(email) }
        .distinctUntilChanged()

    private val isLoadingFlow = state
        .map { it.isLoading }
        .distinctUntilChanged()

    private fun observeFormInputs() {
        combine(
            isEmailValidFlow,
            isLoadingFlow
        ) { isEmailValid, isLoading ->
            _state.update {
                it.copy(canSubmit = isEmailValid && !isLoading)
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: ForgotPasswordAction) {
        when (action) {
            ForgotPasswordAction.OnSubmitClick -> submit()
        }
    }

    private fun submit() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val email = state.value.emailTextFieldState.text.toString()
            when (val result = authService.forgotPassword(email)) {
                is Result.Failure -> {
                    _state.update { it.copy(error = result.toUiText()) }
                }
                is Result.Success -> {
                    _state.update { it.copy(isEmailSentSuccessfully = true) }
                }
            }

            _state.update { it.copy(isLoading = false) }
        }
    }

}