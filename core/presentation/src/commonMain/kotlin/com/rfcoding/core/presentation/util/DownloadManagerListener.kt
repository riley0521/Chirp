package com.rfcoding.core.presentation.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object DownloadManagerListener {

    private val eventChannel = Channel<Unit>()
    val downloadEvents = eventChannel.receiveAsFlow()

    suspend fun notifyDownloadSuccess() {
        eventChannel.send(Unit)
    }
}