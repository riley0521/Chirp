package com.rfcoding.chat.data.chat

import com.rfcoding.chat.data.chat.dto.websocket.IncomingWebSocketDto
import com.rfcoding.chat.data.chat.dto.websocket.OutgoingWebSocketDto
import com.rfcoding.chat.data.chat.dto.websocket.WebSocketMessageDto
import com.rfcoding.chat.data.mappers.toDomain
import com.rfcoding.chat.data.mappers.toIncomingWebSocketDto
import com.rfcoding.chat.data.network.KtorWebSocketConnector
import com.rfcoding.chat.database.ChirpChatDatabase
import com.rfcoding.chat.domain.chat.ChatConnectionClient
import com.rfcoding.chat.domain.chat.ChatRepository
import com.rfcoding.chat.domain.chat.SendMessage
import com.rfcoding.chat.domain.error.ConnectionError
import com.rfcoding.chat.domain.message.MessageRepository
import com.rfcoding.chat.domain.models.ChatMessage
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.domain.util.EmptyResult
import com.rfcoding.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import kotlin.time.Instant

class WebSocketChatConnectionClient(
    private val connector: KtorWebSocketConnector,
    private val chatRepository: ChatRepository,
    private val chatDb: ChirpChatDatabase,
    private val sessionStorage: SessionStorage,
    private val messageRepository: MessageRepository,
    private val json: Json
): ChatConnectionClient {

    override val chatMessages: Flow<ChatMessage> = connector
        .messages
        .mapNotNull { messageDto ->
            processMessage(messageDto)
        }

    override val connectionState = connector.connectionState

    override suspend fun sendMessage(message: SendMessage): EmptyResult<ConnectionError> {
        val newMessage = OutgoingWebSocketDto.NewMessage(
            messageId = message.id,
            chatId = message.chatId,
            content = message.content
        )
        val webSocketMessage = json.encodeToString(
            WebSocketMessageDto(
                type = newMessage.type.name,
                payload = json.encodeToString(newMessage)
            )
        )

        return when (val result = connector.sendMessage(webSocketMessage)) {
            is Result.Failure -> {
                messageRepository.updateMessageDeliveryStatus(
                    messageId = message.id,
                    status = ChatMessageDeliveryStatus.FAILED
                )
                result
            }
            is Result.Success -> {
                result
            }
        }
    }

    private fun processMessage(messageDto: WebSocketMessageDto): ChatMessage? {
        val result = messageDto.toIncomingWebSocketDto(json)

        when (result) {
            is IncomingWebSocketDto.NewMessage -> {
                return ChatMessage(
                    id = result.id,
                    chatId = result.chatId,
                    senderId = result.senderId,
                    content = result.content,
                    messageType = result.messageType,
                    imageUrls = result.imageUrls,
                    event = result.event?.toDomain(),
                    createdAt = Instant.parse(result.createdAt),
                    deliveryStatus = ChatMessageDeliveryStatus.SENT
                )
            }
            is IncomingWebSocketDto.DeleteMessage -> {}
            is IncomingWebSocketDto.Error -> {}
            is IncomingWebSocketDto.ProfilePictureUpdated -> {}
            is IncomingWebSocketDto.UserTyping -> {}
        }

        return null
    }
}