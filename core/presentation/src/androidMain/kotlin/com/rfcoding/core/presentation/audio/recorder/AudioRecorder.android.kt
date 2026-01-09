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
    private var audioFile: File? = null
    private var durationJob: Job? = null
    private var amplitudeJob: Job? = null

    private val _audioRecordData = MutableStateFlow(AudioRecordData())
    actual val data: StateFlow<AudioRecordData> = _audioRecordData

    companion object {
        private const val MAX_AMPLITUDE_VALUE = 26_000L
    }

    actual fun start(fileName: String) {
        if (_audioRecordData.value.isRecording) {
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

            _audioRecordData.update { AudioRecordData(isRecording = true) }
            startTrackingAmplitudes()
            startTrackingDuration()
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
        }
    }

    actual fun stop(): String {
        if (!_audioRecordData.value.isRecording) {
            return ""
        }

        recorder?.apply {
            stop()
            release()
        }
        cleanup()

        return audioFile?.toUri()?.toString() ?: throw IllegalStateException("No audio file created.")
    }

    private fun startTrackingDuration() {
        durationJob = applicationScope.launch {
            var lastTime = System.currentTimeMillis()
            while (_audioRecordData.value.isRecording) {
                delay(10L)
                val currentTime = System.currentTimeMillis()
                val elapsedTime = currentTime - lastTime

                _audioRecordData.update {
                    it.copy(elapsedDuration = it.elapsedDuration + elapsedTime.milliseconds)
                }
                lastTime = System.currentTimeMillis()
            }
        }
    }

    private fun startTrackingAmplitudes() {
        amplitudeJob = applicationScope.launch {
            while (_audioRecordData.value.isRecording) {
                val amplitude = getAmplitude()
                _audioRecordData.update {
                    it.copy(amplitudes = it.amplitudes + amplitude)
                }
                delay(100L)
            }
        }
    }

    private fun getAmplitude(): Float {
        return if (_audioRecordData.value.isRecording) {
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
        recorder = null
        amplitudeJob?.cancel()
        durationJob?.cancel()
        _audioRecordData.update {
            it.copy(
                amplitudes = emptyList(),
                elapsedDuration = Duration.ZERO,
                isRecording = false
            )
        }
    }
}