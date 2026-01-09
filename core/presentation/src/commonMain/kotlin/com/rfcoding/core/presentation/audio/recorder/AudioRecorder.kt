package com.rfcoding.core.presentation.audio.recorder

import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

expect class AudioRecorder {

    val data: StateFlow<AudioRecordData>

    fun start(fileName: String)

    /**
     * @return Uri string for android, and full path for iOS
     */
    fun stop(): String
}

data class AudioRecordData(
    val amplitudes: List<Float> = emptyList(),
    val elapsedDuration: Duration = Duration.ZERO,
    val isRecording: Boolean = false
)