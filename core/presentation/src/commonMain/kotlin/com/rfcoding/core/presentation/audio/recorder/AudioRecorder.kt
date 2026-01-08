package com.rfcoding.core.presentation.audio.recorder

import kotlinx.coroutines.flow.StateFlow

expect class AudioRecorder {

    val amplitudes: StateFlow<List<Float>>

    fun start(fileName: String)

    /**
     * @return Uri string for android, and full path for iOS
     */
    fun stop(): String

    fun isRecording(): Boolean
}