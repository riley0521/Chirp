package com.rfcoding.core.presentation.files

import android.app.DownloadManager
import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Environment
import androidx.core.net.toUri
import com.rfcoding.core.presentation.util.getFileNameExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

actual class FileManager(
    private val context: Context
) {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)

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

    actual fun delete(value: String) {
        val pathFromUri = value.toUri().path ?: return
        File(pathFromUri).delete()
    }

    actual suspend fun downloadImage(url: String, fileName: String): Boolean {
        val request = DownloadManager.Request(url.toUri())
            .setMimeType("image/${getFileNameExtension(fileName)}")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(fileName)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        downloadManager.enqueue(request)

        return true
    }
}