package com.rfcoding.chat.domain.models

sealed interface MediaProgress {
    class Sending(val bytes: ByteArray, val progress: Float = 0f): MediaProgress
    data class Sent(val publicUrl: String): MediaProgress
    data object Failed: MediaProgress
}