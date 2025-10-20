package com.rfcoding.auth.presentation.register_success

sealed interface RegisterSuccessEvent {
    data object Login: RegisterSuccessEvent
}