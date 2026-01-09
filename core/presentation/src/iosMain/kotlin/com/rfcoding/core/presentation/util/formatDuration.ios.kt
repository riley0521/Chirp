package com.rfcoding.core.presentation.util

import platform.Foundation.NSString
import platform.Foundation.stringWithFormat
import kotlin.time.Duration

actual fun Duration.formatMMSS(): String {
    val totalSeconds = this.inWholeSeconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return NSString.stringWithFormat("%02d:%02d", minutes, seconds)
}