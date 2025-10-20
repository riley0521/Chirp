package com.rfcoding.auth.presentation.register_success

import com.rfcoding.core.presentation.util.UiText

sealed interface RegisterSuccessEvent {
    data object Login: RegisterSuccessEvent
    data object ResendEmailVerificationSuccess: RegisterSuccessEvent
    data class ResendEmailVerificationFailure(val error: UiText): RegisterSuccessEvent
}