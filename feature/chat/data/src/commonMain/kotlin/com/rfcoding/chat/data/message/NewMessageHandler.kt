package com.rfcoding.chat.data.message

import com.rfcoding.chat.domain.message.MessageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class NewMessageHandler(
    private val applicationScope: CoroutineScope,
    private val messageRepository: MessageRepository
) {

    fun handleIncomingMessage(chatId: String, messageId: String, onComplete: () -> Unit = {}) {
        applicationScope.launch {
            messageRepository.fetchMessage(chatId, messageId)
            onComplete()
        }
    }
}