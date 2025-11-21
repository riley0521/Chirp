package com.rfcoding.chat.domain.chat

import com.rfcoding.chat.domain.error.ConnectionError
import com.rfcoding.chat.domain.models.ChatMessage
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.domain.models.ChatMessageType
import com.rfcoding.chat.domain.models.ConnectionState
import com.rfcoding.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Clock
import kotlin.time.Instant

data class SendMessage(
    val id: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val createdAt: Instant,
    val messageType: ChatMessageType = ChatMessageType.MESSAGE_TEXT,
    val imageUrl: List<String> = emptyList(),
    val deliveryStatus: ChatMessageDeliveryStatus = ChatMessageDeliveryStatus.SENDING
)

data class UserTypingData(
    val userId: String,
    val chatId: String,
    val typedAt: Instant = Clock.System.now()
)

interface ChatConnectionClient {
    val chatMessages: Flow<ChatMessage>
    val connectionState: StateFlow<ConnectionState>
    val usersTypingState: StateFlow<List<UserTypingData>>
    suspend fun sendMessage(message: SendMessage): EmptyResult<ConnectionError>
    suspend fun sendTypingIndicator(chatId: String)
}