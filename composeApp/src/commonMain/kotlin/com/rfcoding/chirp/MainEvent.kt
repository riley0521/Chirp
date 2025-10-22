package com.rfcoding.chirp

sealed interface MainEvent {
    data object OnSessionExpired: MainEvent
}