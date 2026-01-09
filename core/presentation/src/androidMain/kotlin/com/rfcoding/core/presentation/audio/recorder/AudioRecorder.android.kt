package com.rfcoding.core.presentation.audio.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

actual class AudioRecorder(
    private val context: Context,
    private val applicationScope: CoroutineScope
) {

    private var recorder: MediaRecorder? = null
    private var isRecording = false
    private var audioFile: File? = null
    private var durationJob: Job? = null
    private var amplitudeJob: Job? = null

    private val _audioRecordData = MutableStateFlow<AudioRecordData?>(null)
    actual val data: StateFlow<AudioRecordData?> = _audioRecordData

    companion object {
        private const val MAX_AMPLITUDE_VALUE = 26_000L
    }

    actual fun start(fileName: String) {
        if (isRecording) {
            return
        }

        try {

            audioFile = createFileFromFileName(fileName)
            recorder = newMediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128_000)
                setAudioSamplingRate(44100)
                setOutputFile(audioFile?.absolutePath)

                prepare()
                start()
            }

            isRecording = true

            _audioRecordData.update { AudioRecordData() }
            startTrackingAmplitudes()
            startTrackingDuration()
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
        }
    }

    actual fun stop(): String {
        if (!isRecording) {
            return ""
        }

        recorder?.apply {
            stop()
            release()
        }
        cleanup()

        return audioFile?.toUri()?.toString() ?: throw IllegalStateException("No audio file created.")
    }

    actual fun isRecording(): Boolean = isRecording

    private fun startTrackingDuration() {
        durationJob = applicationScope.launch {
            var lastTime = System.currentTimeMillis()
            while (isRecording) {
                delay(10L)
                val currentTime = System.currentTimeMillis()
                val elapsedTime = currentTime - lastTime

                _audioRecordData.update {
                    val updatedDuration = it
                        ?.elapsedDuration
                        ?.plus(elapsedTime.milliseconds) ?: Duration.ZERO
                    it?.copy(elapsedDuration = updatedDuration)
                }
                lastTime = System.currentTimeMillis()
            }
        }
    }

    private fun startTrackingAmplitudes() {
        amplitudeJob = applicationScope.launch {
            while (isRecording) {
                val amplitude = getAmplitude()

                _audioRecordData.update {
                    val updatedAmplitudes = it?.amplitudes?.plus(amplitude).orEmpty()
                    it?.copy(amplitudes = updatedAmplitudes)
                }
                delay(100L)
            }
        }
    }

    private fun getAmplitude(): Float {
        return if (isRecording) {
            try {
                val maxAmplitude = recorder?.maxAmplitude
                val amplitudeRatio = maxAmplitude?.takeIf { it > 0 }?.run {
                    (this / MAX_AMPLITUDE_VALUE.toFloat()).coerceIn(0f, 1f)
                }
                amplitudeRatio ?: 0f
            } catch (e: Exception) {
                0f
            }
        } else 0f
    }

    private fun createFileFromFileName(fileName: String): File {
        return File(
            context.cacheDir,
            fileName
        )
    }

    private fun newMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    private fun cleanup() {
        isRecording = false
        recorder = null
        amplitudeJob?.cancel()
        durationJob?.cancel()
        _audioRecordData.update { null }
    }
}