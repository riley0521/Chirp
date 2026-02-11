package com.rfcoding.auth.presentation.forgot_password

sealed interface ForgotPasswordAction {
    data object OnSubmitClick: ForgotPasswordAction
    data object OnLoginClick: ForgotPasswordAction
}