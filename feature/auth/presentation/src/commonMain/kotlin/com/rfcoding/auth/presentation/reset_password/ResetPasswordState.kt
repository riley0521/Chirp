package com.rfcoding.auth.presentation.reset_password

import androidx.compose.foundation.text.input.TextFieldState
import com.rfcoding.core.presentation.util.UiText

data class ResetPasswordState(
    val passwordTextFieldState: TextFieldState = TextFieldState(),
    val passwordError: UiText? = null,
    val isPasswordVisible: Boolean = false,
    val error: UiText? = null,
    val canSubmit: Boolean = false,
    val isLoading: Boolean = false,
    val isResetPasswordSuccessful: Boolean = false
)