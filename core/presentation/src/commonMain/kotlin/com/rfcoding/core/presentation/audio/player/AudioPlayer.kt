package com.rfcoding.core.presentation.audio.player

import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

expect class AudioPlayer {

    val activeTrack: StateFlow<AudioTrack?>

    /**
     * @param path is mostly remote URL. If local, it's string URI for android, and full path for iOS
     * @param totalDuration is saved per voice message and we can just pass it here.
     */
    fun play(path: String, totalDuration: Duration? = null)
    fun pause()
    fun resume()

    /**
     * Set playback complete listener to be notified when the audio is finished playing.
     */
    fun setOnPlaybackCompleteListener(listener: () -> Unit)

    /**
     * Seek to different position from playback.
     */
    fun seekTo(position: Duration)
}

data class AudioTrack(
    val totalDuration: Duration = Duration.ZERO,
    val durationPlayed: Duration = Duration.ZERO,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false
)