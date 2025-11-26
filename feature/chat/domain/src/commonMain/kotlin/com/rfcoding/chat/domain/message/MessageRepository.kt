package com.rfcoding.chat.domain.message

import com.rfcoding.chat.domain.models.MessageWithSender
import com.rfcoding.chat.domain.models.OutgoingNewMessage
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface MessageRepository {

    suspend fun fetchMessages(
        chatId: String,
        before: String? = null
    ): EmptyResult<DataError>

    fun getMessagesForChat(chatId: String): Flow<List<MessageWithSender>>

    suspend fun sendMessage(message: OutgoingNewMessage): EmptyResult<DataError>

    suspend fun retryMessage(messageId: String): EmptyResult<DataError>

    suspend fun deleteMessage(messageId: String): EmptyResult<DataError>
}