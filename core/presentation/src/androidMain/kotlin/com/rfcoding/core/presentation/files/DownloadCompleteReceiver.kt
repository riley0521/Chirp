package com.rfcoding.core.presentation.files

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rfcoding.core.presentation.util.DownloadManagerListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DownloadCompleteReceiver: BroadcastReceiver(), KoinComponent {

    private val applicationScope: CoroutineScope by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.DOWNLOAD_COMPLETE") {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (id != -1L) {
                applicationScope.launch { DownloadManagerListener.notifyDownloadSuccess() }
            }
        }
    }
}