package com.rfcoding.chat.data.notification

import com.rfcoding.chat.data.message.NewMessageHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object IosDeviceTokenHolder: KoinComponent {

    private val _token = MutableStateFlow<String?>(null)
    val token = _token.asStateFlow()

    fun updateToken(token: String?) {
        _token.value = token
    }

    private val newMessageHandler by inject<NewMessageHandler>()

    fun processNewMessage(chatId: String, messageId: String, onComplete: () -> Unit = {}) {
        newMessageHandler.handleIncomingMessage(chatId, messageId, onComplete)
    }
}