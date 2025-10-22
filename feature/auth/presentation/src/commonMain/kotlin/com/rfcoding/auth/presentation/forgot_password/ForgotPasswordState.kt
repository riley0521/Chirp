package com.rfcoding.auth.presentation.forgot_password

import androidx.compose.foundation.text.input.TextFieldState
import com.rfcoding.core.presentation.util.UiText

data class ForgotPasswordState(
    val emailTextFieldState: TextFieldState = TextFieldState(),
    val canSubmit: Boolean = false,
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val isEmailSentSuccessfully: Boolean = false
)