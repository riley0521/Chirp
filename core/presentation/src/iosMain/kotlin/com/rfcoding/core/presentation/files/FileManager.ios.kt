package com.rfcoding.core.presentation.files

import com.rfcoding.core.presentation.util.formatUrl
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.AVFoundation.AVURLAsset
import platform.CoreMedia.CMTimeGetSeconds
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum
import platform.posix.memcpy
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalForeignApi::class)
actual class FileManager {

    actual suspend fun getBytes(value: String): ByteArray? {
        val url = NSURL.fileURLWithPath(value)
        val nsData = NSData.dataWithContentsOfURL(url) ?: return null

        val bytes = ByteArray(nsData.length.toInt())
        withContext(Dispatchers.Default) {
            memcpy(bytes.refTo(0), nsData.bytes, nsData.length)
        }

        return bytes
    }

    actual fun getAudioDuration(value: String): Duration {
        val url = formatUrl(value)
        val asset = AVURLAsset(url, null)
        val durationMs = (CMTimeGetSeconds(asset.duration) * 1000).toLong()
        return durationMs.milliseconds
    }

    actual fun delete(value: String) {
        NSFileManager.defaultManager.removeItemAtPath(value, null)
    }

    actual suspend fun downloadImage(url: String, fileName: String): Boolean = withContext(Dispatchers.IO) {
        val data = NSURL(string = url)
                .let { NSData.dataWithContentsOfURL(it) }
                ?: return@withContext false

        val image = UIImage(data = data)
        UIImageWriteToSavedPhotosAlbum(image, null, null, null)

        true
    }
}