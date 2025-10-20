package com.rfcoding.auth.presentation.register_success

data class RegisterSuccessState(
    val registeredEmail: String = "",
    val isResendingEmailVerification: Boolean = false
)