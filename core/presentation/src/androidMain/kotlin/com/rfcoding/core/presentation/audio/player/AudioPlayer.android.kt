package com.rfcoding.core.presentation.audio.player

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import com.rfcoding.core.presentation.files.FileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

actual class AudioPlayer(
    private val context: Context,
    private val fileManager: FileManager,
    private val applicationScope: CoroutineScope
) {

    private var mediaPlayer: MediaPlayer? = null
    private var onPlaybackComplete: (() -> Unit)? = null
    private var durationJob: Job? = null

    private val _activeTrack = MutableStateFlow<AudioTrack?>(null)
    actual val activeTrack: StateFlow<AudioTrack?> = _activeTrack

    actual fun play(path: String) {
        release()

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(context, path.toUri())
                prepare()
                start()

                _activeTrack.update {
                    AudioTrack(
                        totalDuration = fileManager.getAudioDuration(path),
                        durationPlayed = Duration.ZERO,
                        isPlaying = true
                    )
                }
                startTrackingDuration()

                setOnCompletionListener {
                    onPlaybackComplete?.invoke()
                    stop()
                }
            } catch (e: Exception) {

            }
        }
    }

    actual fun pause() {
        mediaPlayer?.pause()
        durationJob?.cancel()
        _activeTrack.update { it?.copy(isPlaying = false) }
    }

    actual fun resume() {
        mediaPlayer?.start()
        startTrackingDuration()
        _activeTrack.update { it?.copy(isPlaying = true) }
    }

    private fun release() {
        _activeTrack.update { null }
        durationJob?.cancel()
        mediaPlayer?.apply {
            stop()
            reset()
            release()
        }
        mediaPlayer = null
    }

    private fun startTrackingDuration() {
        durationJob = applicationScope.launch {
            do {
                _activeTrack.update {
                    it?.copy(
                        durationPlayed = mediaPlayer?.currentPosition?.milliseconds ?: Duration.ZERO
                    )
                }
                delay(10L)
            } while (_activeTrack.value?.isPlaying == true && mediaPlayer?.isPlaying == true)
        }
    }

    actual fun setOnPlaybackCompleteListener(listener: () -> Unit) {
        onPlaybackComplete = listener
    }

    actual fun seekTo(position: Duration) {
        mediaPlayer?.seekTo(position.toInt(DurationUnit.MILLISECONDS))
    }
}