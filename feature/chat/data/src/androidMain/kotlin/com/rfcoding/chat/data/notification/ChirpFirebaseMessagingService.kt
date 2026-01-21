package com.rfcoding.chat.data.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rfcoding.chat.data.message.NewMessageHandler
import com.rfcoding.chat.domain.notification.DeviceTokenService
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.domain.logging.ChirpLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ChirpFirebaseMessagingService: FirebaseMessagingService() {

    private val deviceTokenService by inject<DeviceTokenService>()
    private val sessionStorage by inject<SessionStorage>()
    private val applicationScope by inject<CoroutineScope>()
    private val newMessageHandler by inject<NewMessageHandler>()
    private val logger by inject<ChirpLogger>()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        applicationScope.launch {
            sessionStorage.observeAuthenticatedUser().first() ?: return@launch
            deviceTokenService.registerToken(
                token = token,
                platform = "ANDROID"
            )
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        logger.debug("Message received: " + message.data.toString())

        val chatId = message.data["chatId"] ?: return
        val messageId = message.data["messageId"] ?: return

        newMessageHandler.handleIncomingMessage(chatId, messageId)
    }
}