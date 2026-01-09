package com.rfcoding.core.presentation.util

import java.util.Locale
import kotlin.time.Duration

actual fun Duration.formatMMSS(): String {
    val totalSeconds = this.inWholeSeconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return String.format(
        locale = Locale.getDefault(),
        "%02d:%02d",
        minutes,
        seconds
    )
}