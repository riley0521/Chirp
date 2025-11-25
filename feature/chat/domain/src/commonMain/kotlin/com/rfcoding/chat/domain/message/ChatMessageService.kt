package com.rfcoding.chat.domain.message

import com.rfcoding.chat.domain.models.ChatMessage
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.Result

interface ChatMessageService {

    suspend fun fetchMessages(
        chatId: String,
        before: String? = null
    ): Result<List<Pair<ChatMessage, List<String>?>>, DataError.Remote>
}