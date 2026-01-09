package com.rfcoding.core.presentation.audio.player

import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

expect class AudioPlayer {

    val activeTrack: StateFlow<AudioTrack?>

    fun play(path: String)
    fun pause()
    fun resume()
    fun setOnPlaybackCompleteListener(listener: () -> Unit)
    fun seekTo(position: Duration)
}

data class AudioTrack(
    val totalDuration: Duration = Duration.ZERO,
    val durationPlayed: Duration = Duration.ZERO,
    val isPlaying: Boolean = false
)