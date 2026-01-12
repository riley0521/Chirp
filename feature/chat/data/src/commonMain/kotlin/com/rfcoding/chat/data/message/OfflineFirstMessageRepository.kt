package com.rfcoding.chat.data.message

import com.rfcoding.chat.data.chat.dto.websocket.OutgoingWebSocketDto
import com.rfcoding.chat.data.chat.dto.websocket.OutgoingWebSocketType
import com.rfcoding.chat.data.chat.dto.websocket.WebSocketMessageDto
import com.rfcoding.chat.data.network.KtorWebSocketConnector
import com.rfcoding.chat.database.ChirpChatDatabase
import com.rfcoding.chat.database.entities.ChatMediaEntity
import com.rfcoding.chat.database.entities.ChatMessageEntity
import com.rfcoding.chat.database.mapper.toDomain
import com.rfcoding.chat.database.mapper.toEntity
import com.rfcoding.chat.database.model.MediaStatus
import com.rfcoding.chat.domain.message.ChatMessageService
import com.rfcoding.chat.domain.message.MessageRepository
import com.rfcoding.chat.domain.models.ChatMessage
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.domain.models.ChatMessageType
import com.rfcoding.chat.domain.models.Media
import com.rfcoding.chat.domain.models.MediaProgress
import com.rfcoding.chat.domain.models.MediaType
import com.rfcoding.chat.domain.models.MessageWithSender
import com.rfcoding.chat.domain.models.OutgoingNewMessage
import com.rfcoding.core.data.database.safeDatabaseUpdate
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
import com.rfcoding.core.domain.util.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class OfflineFirstMessageRepository(
    private val connector: KtorWebSocketConnector,
    private val chatMessageService: ChatMessageService,
    private val chatDb: ChirpChatDatabase,
    private val sessionStorage: SessionStorage,
    private val json: Json,
    private val applicationScope: CoroutineScope
): MessageRepository {

    override suspend fun fetchMessages(
        chatId: String,
        before: String?
    ): Result<List<ChatMessage>, DataError> = supervisorScope {
        return@supervisorScope safeDatabaseUpdate {
            return@supervisorScope when (val result = chatMessageService.fetchMessages(chatId, before)) {
                is Result.Failure -> result
                is Result.Success -> {
                    val chatEntities = result.data.map { (message, affectedUserIds) ->
                        message.toEntity(affectedUserIds.orEmpty())
                    }
                    chatDb.chatMessageDao.upsertMessagesAndSyncIfNecessary(
                        chatId = chatId,
                        serverMessages = chatEntities,
                        // Server response of messages may differ in size in the future.
                        // So, this is for safer approach.
                        pageSize = chatEntities.size,
                        shouldSync = before == null // Only sync for most recent page
                    )
                    val chatsDeferred = chatEntities.map { entity ->
                        async {
                            val affectedUsernames = chatDb
                                .chatParticipantDao
                                .getUsernamesByUserIds(
                                    entity.event?.affectedUserIds.orEmpty()
                                ).map { it.username }

                            entity.toDomain(affectedUsernames)
                        }
                    }

                    Result.Success(chatsDeferred.awaitAll())
                }
            }
        }
    }

    override fun getMessagesForChat(chatId: String): Flow<List<MessageWithSender>> {
        return chatDb.chatMessageDao.getMessagesByChatId(chatId).map { messages ->
            supervisorScope {
                val mappedMessages = messages.map { messageWithSender ->
                    async {
                        val affectedUsernames = chatDb.chatParticipantDao.getUsernamesByUserIds(
                            messageWithSender.message.event?.affectedUserIds.orEmpty()
                        ).map { it.username }
                        messageWithSender.toDomain(affectedUsernames)
                    }
                }

                mappedMessages.awaitAll()
            }
        }
    }

    private suspend fun getUserId(): String {
        return sessionStorage
            .observeAuthenticatedUser()
            .firstOrNull()
            ?.user?.id ?: throw IllegalStateException("User is not logged in.")
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun sendLocalMessage(
        message: OutgoingNewMessage,
        imagesToUpload: List<ByteArray>,
        audioBytes: ByteArray?,
        audioDurationInSeconds: Int
    ): Result<List<Media>, DataError> {
        val chatMessageType = when {
            imagesToUpload.isNotEmpty() -> {
                ChatMessageType.MESSAGE_TEXT_WITH_IMAGES
            }
            audioBytes != null -> {
                ChatMessageType.MESSAGE_VOICE_OVER_ONLY
            }
            else -> ChatMessageType.MESSAGE_TEXT
        }

        val newMessage = ChatMessageEntity(
            id = message.messageId,
            chatId = message.chatId,
            senderId = getUserId(),
            content = message.content,
            chatMessageType = chatMessageType,
            imageUrls = emptyList(),
            event = null,
            createdAt = Clock.System.now(),
            deliveryStatus = ChatMessageDeliveryStatus.SENDING,
            audioDurationInSeconds = audioDurationInSeconds
        )

        val upsertMessageResult = safeDatabaseUpdate {
            chatDb.chatMessageDao.upsertMessage(newMessage)
        }
        if (upsertMessageResult is Result.Failure) {
            return upsertMessageResult
        }

        return safeDatabaseUpdate {
            when (chatMessageType) {
                ChatMessageType.MESSAGE_TEXT_WITH_IMAGES -> {
                    val imageMediaList = imagesToUpload.map {
                        ChatMediaEntity(
                            messageId = message.messageId,
                            name = Uuid.random().toString(),
                            bytes = it,
                            progress = 0f,
                            type = MediaType.IMAGE
                        )
                    }
                    chatDb.chatMediaDao.upsertMedias(imageMediaList)

                    imageMediaList.map { it.toDomain() }
                }
                ChatMessageType.MESSAGE_VOICE_OVER_ONLY -> {
                    val audio = ChatMediaEntity(
                        messageId = message.messageId,
                        name = Uuid.random().toString(),
                        bytes = audioBytes!!,
                        progress = 0f,
                        type = MediaType.AUDIO
                    )
                    chatDb.chatMediaDao.upsertMedia(audio)

                    listOf(audio.toDomain())
                }
                else -> emptyList()
            }
        }

//        val rawMessage = json.encodeToString(
//            WebSocketMessageDto(
//                type = OutgoingWebSocketType.NEW_MESSAGE.name,
//                payload = json.encodeToString(message.toDto(uploadedImageUrls = emptyList(), audioDurationInSeconds = 0))
//            )
//        )
//
//        return when (val result = connector.sendMessage(rawMessage)) {
//            is Result.Failure -> {
//                applicationScope.launch {
//                    chatDb.chatMessageDao.updateDeliveryStatus(
//                        id = message.messageId,
//                        deliveryStatus = ChatMessageDeliveryStatus.FAILED,
//                        deliveredAt = Clock.System.now()
//                    )
//                }.join()
//
//                result
//            }
//            is Result.Success -> {
//                chatDb.chatMessageDao.updateDeliveryStatus(
//                    id = message.messageId,
//                    deliveryStatus = ChatMessageDeliveryStatus.SENT,
//                    deliveredAt = Clock.System.now()
//                )
//
//                result
//            }
//        }
    }

    override suspend fun sendMessage(messageId: String): EmptyResult<DataError> {
        val message = chatDb.chatMessageDao.getById(messageId)
            ?: return Result.Failure(DataError.Local.NOT_FOUND)

        val rawMessage = json.encodeToString(
            WebSocketMessageDto(
                type = OutgoingWebSocketType.NEW_MESSAGE.name,
                payload = json.encodeToString(
                    OutgoingWebSocketDto.NewMessage(
                        messageId = messageId,
                        chatId = message.chatId,
                        content = message.content,
                        messageType = message.chatMessageType,
                        uploadedImageUrls = message.imageUrls,
                        audioDurationInSeconds = message.audioDurationInSeconds
                    )
                )
            )
        )

        return when (val result = connector.sendMessage(rawMessage)) {
            is Result.Failure -> {
                changeDeliveryStatusOfLocalMessage(
                    messageId = messageId,
                    status = ChatMessageDeliveryStatus.FAILED
                )

                result
            }
            is Result.Success -> {
                changeDeliveryStatusOfLocalMessage(
                    messageId = messageId,
                    status = ChatMessageDeliveryStatus.SENT
                )

                result
            }
        }
    }

    override suspend fun updateMediaProgress(
        messageId: String,
        name: String,
        progress: MediaProgress
    ): EmptyResult<DataError> {
        return safeDatabaseUpdate {
            val media = chatDb.chatMediaDao.getByName(name)
                ?: return Result.Failure(DataError.Local.NOT_FOUND)
            val message = chatDb.chatMessageDao.getById(messageId)
                ?: return Result.Failure(DataError.Local.NOT_FOUND)

            when (progress) {
                MediaProgress.Failed -> {
                    chatDb.chatMediaDao.upsertMedia(
                        media.copy(status = MediaStatus.FAILED)
                    )
                }
                is MediaProgress.Sending -> {
                    chatDb.chatMediaDao.upsertMedia(
                        media.copy(status = MediaStatus.SENDING, progress = progress.progress)
                    )
                }
                is MediaProgress.Sent -> {
                    when (media.type) {
                        MediaType.IMAGE -> {
                            chatDb.chatMessageDao.upsertMessage(
                                message.copy(
                                    imageUrls = message.imageUrls + progress.publicUrl
                                )
                            )
                        }
                        MediaType.AUDIO -> {
                            chatDb.chatMessageDao.upsertMessage(
                                message.copy(
                                    content = progress.publicUrl
                                )
                            )
                        }
                    }

                    chatDb.chatMediaDao.deleteById(media.id)
                }
            }
        }
    }

    override suspend fun getPendingMedias(messageId: String): List<Media> {
        return chatDb.chatMediaDao.getByMessageId(messageId).map { it.toDomain() }
    }

    override suspend fun changeDeliveryStatusOfLocalMessage(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ) {
        applicationScope.launch {
            chatDb.chatMessageDao.updateDeliveryStatus(
                id = messageId,
                deliveryStatus = status,
                deliveredAt = Clock.System.now()
            )
        }.join()
    }

    override suspend fun deleteMessage(messageId: String): EmptyResult<DataError> {
        return chatMessageService.deleteMessage(messageId)
    }
}