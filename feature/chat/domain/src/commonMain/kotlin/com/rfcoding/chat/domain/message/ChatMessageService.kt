package com.rfcoding.chat.domain.message

import com.rfcoding.chat.domain.models.ChatMessage
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
import com.rfcoding.core.domain.util.Result

interface ChatMessageService {

    suspend fun fetchMessages(
        chatId: String,
        before: String? = null
    ): Result<List<Pair<ChatMessage, List<String>?>>, DataError.Remote>

    suspend fun fetchMessage(
        messageId: String
    ): Result<Pair<ChatMessage, List<String>?>, DataError.Remote>

    suspend fun deleteMessage(
        messageId: String
    ): EmptyResult<DataError.Remote>

    /**
     * @return Public URL of the image.
     */
    suspend fun uploadFile(
        chatId: String,
        bytes: ByteArray
    ): Result<String, DataError.Remote>
}