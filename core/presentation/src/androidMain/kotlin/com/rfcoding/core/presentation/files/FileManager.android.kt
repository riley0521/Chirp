package com.rfcoding.core.presentation.files

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

actual class FileManager(
    private val context: Context
) {

    actual suspend fun getBytes(value: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(value.toUri())?.use {
                it.readBytes()
            }
        }
    }

    actual fun getAudioDuration(value: String): Duration {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, value.toUri())
        val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
        return durationMs.milliseconds
    }
}