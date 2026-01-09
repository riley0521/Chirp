package com.rfcoding.core.presentation.audio.player

import com.rfcoding.core.presentation.files.FileManager
import com.rfcoding.core.presentation.util.formatUrl
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.seekToTime
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.darwin.NSObjectProtocol
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

actual class AudioPlayer(
    private val fileManager: FileManager,
    private val applicationScope: CoroutineScope
) {

    private var player: AVPlayer? = null
    private var playerObserver: NSObjectProtocol? = null
    private var onPlaybackComplete: (() -> Unit)? = null

    private val _activeTrack = MutableStateFlow<AudioTrack?>(null)
    actual val activeTrack: StateFlow<AudioTrack?> = _activeTrack

    private var durationJob: Job? = null

    @OptIn(ExperimentalForeignApi::class)
    actual fun play(path: String) {
        try {
            release()

            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryPlayback, null)
            audioSession.setActive(true, null)
            val nsUrl = formatUrl(path)
            player = AVPlayer.playerWithURL(nsUrl)

            // Setup notification observer for playback completion.
            setupPlaybackObserver()
            player?.play()

            _activeTrack.update {
                AudioTrack(
                    totalDuration = fileManager.getAudioDuration(path),
                    durationPlayed = Duration.ZERO,
                    isPlaying = true
                )
            }
            startTrackingDuration()
        } catch (e: Exception) {

        }
    }

    actual fun pause() {
        player?.pause()
        durationJob?.cancel()
        _activeTrack.update { it?.copy(isPlaying = false) }
    }

    actual fun resume() {
        player?.play()
        startTrackingDuration()
        _activeTrack.update { it?.copy(isPlaying = true) }
    }

    private fun release() {
        removePlaybackObserver()

        _activeTrack.update { null }
        durationJob?.cancel()
        player?.pause()
        player = null
    }

    private fun setupPlaybackObserver() {
        removePlaybackObserver()

        playerObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = player?.currentItem,
            queue = null
        ) { _ ->
            onPlaybackComplete?.invoke()
        }
    }

    private fun removePlaybackObserver() {
        playerObserver?.run {
            NSNotificationCenter.defaultCenter.removeObserver(this)
            playerObserver = null
        }
    }

    actual fun setOnPlaybackCompleteListener(listener: () -> Unit) {
        onPlaybackComplete = listener
    }

    private fun startTrackingDuration() {
        durationJob = applicationScope.launch {
            do {
                _activeTrack.update {
                    it?.copy(
                        durationPlayed = getCurrentPosition()
                    )
                }
                delay(10L)
            } while (_activeTrack.value?.isPlaying == true)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getCurrentPosition(): Duration {
        return player?.run {
            val seconds = CMTimeGetSeconds(currentTime())
            (seconds * 1000).toLong().milliseconds
        } ?: Duration.ZERO
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun seekTo(position: Duration) {
        player?.run {
            val seconds = position.inWholeSeconds
            val time = CMTimeMakeWithSeconds(seconds.toDouble(), 1000)
            seekToTime(time)
        }
    }
}