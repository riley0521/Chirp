package com.rfcoding.chat.domain.message

import com.rfcoding.chat.domain.models.ChatMessage
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.domain.models.Media
import com.rfcoding.chat.domain.models.MediaProgress
import com.rfcoding.chat.domain.models.MessageWithSender
import com.rfcoding.chat.domain.models.OutgoingNewMessage
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
import com.rfcoding.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface MessageRepository {

    suspend fun fetchMessages(
        chatId: String,
        before: String? = null
    ): Result<List<ChatMessage>, DataError>

    fun getMessagesForChat(chatId: String): Flow<List<MessageWithSender>>

    suspend fun sendLocalMessage(
        message: OutgoingNewMessage,
        imagesToUpload: List<ByteArray> = emptyList(),
        audioBytes: ByteArray? = null,
        audioDurationInSeconds: Int = 0
    ): Result<List<Media>, DataError>

    suspend fun sendMessage(messageId: String): EmptyResult<DataError>

    suspend fun updateMediaProgress(messageId: String, name: String, progress: MediaProgress): EmptyResult<DataError>

    suspend fun getPendingMedias(messageId: String): List<Media>

    suspend fun changeDeliveryStatusOfLocalMessage(messageId: String, status: ChatMessageDeliveryStatus)

    suspend fun deleteMessage(messageId: String): EmptyResult<DataError>
}