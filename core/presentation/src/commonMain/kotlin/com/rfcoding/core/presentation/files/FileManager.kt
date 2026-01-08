package com.rfcoding.core.presentation.files

import kotlin.time.Duration

expect class FileManager {

    /**
     * Pass uri for android here, or else it will return null.
     */
    suspend fun getBytes(value: String): ByteArray?

    fun getAudioDuration(value: String): Duration

    fun delete(value: String)
}