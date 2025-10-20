package com.rfcoding.auth.presentation.email_verification

sealed interface EmailVerificationEvent {
    data object Login: EmailVerificationEvent
    data object Close: EmailVerificationEvent
}