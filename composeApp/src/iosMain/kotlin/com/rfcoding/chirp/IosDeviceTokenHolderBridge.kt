package com.rfcoding.chirp

import com.rfcoding.chat.data.notification.IosDeviceTokenHolder

object IosDeviceTokenHolderBridge {

    fun updateToken(token: String?) {
        IosDeviceTokenHolder.updateToken(token)
    }

    fun processNewMessage(chatId: String, messageId: String, onComplete: () -> Unit = {}) {
        IosDeviceTokenHolder.processNewMessage(chatId, messageId, onComplete)
    }
}