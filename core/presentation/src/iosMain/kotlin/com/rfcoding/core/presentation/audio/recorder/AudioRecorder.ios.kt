package com.rfcoding.core.presentation.audio.recorder

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioQualityHigh
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVEncoderBitRateKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.posix.pow

actual class AudioRecorder(
    private val applicationScope: CoroutineScope
) {

    private var recorder: AVAudioRecorder? = null
    private var amplitudeJob: Job? = null

    private var audioUrl: NSURL? = null

    private val _amplitudes = MutableStateFlow<List<Float>>(emptyList())
    actual val amplitudes: StateFlow<List<Float>> = _amplitudes

    @OptIn(ExperimentalForeignApi::class)
    actual fun start(fileName: String) {
        val fileManager = NSFileManager.defaultManager
        val documentsDir = fileManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        requireNotNull(documentsDir)

        audioUrl = documentsDir.URLByAppendingPathComponent(fileName)

        val audioSession = AVAudioSession.sharedInstance()
        audioSession.setCategory(AVAudioSessionCategoryRecord, null)
        audioSession.setActive(true, null)

        val settings = mapOf<Any?, Any>(
            AVFormatIDKey to kAudioFormatMPEG4AAC,
            AVSampleRateKey to 44100.0,
            AVNumberOfChannelsKey to 2,
            AVEncoderAudioQualityKey to AVAudioQualityHigh,
            AVEncoderBitRateKey to 128_000,
            "AVEnableRecordingMetering" to true
        )

        recorder = AVAudioRecorder(
            uRL = audioUrl!!,
            settings = settings,
            error = null
        ).apply {
            prepareToRecord()
            meteringEnabled = true
            record()
        }
        startTrackingAmplitudes()
    }

    actual fun stop(): String {
        if (!isRecording()) {
            return ""
        }

        recorder?.stop()
        recorder = null
        amplitudeJob?.cancel()

        return audioUrl?.path ?: throw IllegalStateException("No audio file created.")
    }

    actual fun isRecording(): Boolean {
        return recorder?.recording == true
    }

    private fun startTrackingAmplitudes() {
        amplitudeJob = applicationScope.launch {
            while (isRecording()) {
                val amplitude = getAmplitude()
                _amplitudes.update { it + amplitude }
                delay(100L)
            }
        }
    }

    private fun getAmplitude(): Float {
        return recorder?.run {
            updateMeters()

            val channel0 = peakPowerForChannel(0u)
            val channel1 = peakPowerForChannel(1u)

            val maxPower = maxOf(channel0, channel1)

            if (maxPower.isFinite()) {
                val normalized = pow(10.0, maxPower / 20.0).toFloat()
                normalized.coerceIn(0f, 1f)
            } else 0f
        } ?: 0f
    }
}