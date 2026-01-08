package com.rfcoding.core.presentation.files

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.AVFoundation.AVURLAsset
import platform.CoreMedia.CMTimeGetSeconds
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.posix.memcpy
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

actual class FileManager {

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getBytes(value: String): ByteArray? {
        val url = NSURL.fileURLWithPath(value)
        val nsData = NSData.dataWithContentsOfURL(url)
        if (nsData == null) {
            return null
        }

        val bytes = ByteArray(nsData.length.toInt())
        withContext(Dispatchers.Default) {
            memcpy(bytes.refTo(0), nsData.bytes, nsData.length)
        }

        return bytes
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun getAudioDuration(value: String): Duration {
        val url = formatUrl(value)
        val asset = AVURLAsset(url, null)
        val durationMs = (CMTimeGetSeconds(asset.duration) * 1000).toLong()
        return durationMs.milliseconds
    }

    private fun formatUrl(url: String): NSURL {
        return when {
            url.startsWith("file://") || url.startsWith("http") -> NSURL(string = url)
            else -> NSURL.fileURLWithPath(url)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun delete(value: String) {
        NSFileManager.defaultManager.removeItemAtPath(value, null)
    }
}