package com.rfcoding.auth.presentation.login

sealed interface LoginEvent {
    data object ForgotPassword: LoginEvent
    data object LoginSuccessful: LoginEvent
    data object Register: LoginEvent
}