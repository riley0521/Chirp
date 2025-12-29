package com.rfcoding.chat.data.chat

import com.rfcoding.chat.database.ChirpChatDatabase
import com.rfcoding.chat.database.entities.ChatWithParticipantsEntity
import com.rfcoding.chat.database.mapper.toDatabaseView
import com.rfcoding.chat.database.mapper.toDomain
import com.rfcoding.chat.database.mapper.toEntity
import com.rfcoding.chat.domain.chat.ChatRepository
import com.rfcoding.chat.domain.chat.ChatService
import com.rfcoding.chat.domain.models.Chat
import com.rfcoding.chat.domain.models.ChatInfo
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
import com.rfcoding.core.domain.util.Result
import com.rfcoding.core.domain.util.asEmptyResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope

class OfflineFirstChatRepository(
    private val chatService: ChatService,
    private val chatDb: ChirpChatDatabase,
    private val sessionStorage: SessionStorage
): ChatRepository {

    override fun getAllChats(): Flow<List<Chat>> {
        return chatDb.chatDao.getChatsWithParticipants().map { chatsWithParticipants ->
            supervisorScope {
                val chatsDeferred = chatsWithParticipants.map {
                    async {
                        it.toChatWithAffectedUsernames()
                    }
                }

                chatsDeferred.awaitAll()
            }
        }
    }

    private suspend fun ChatWithParticipantsEntity.toChatWithAffectedUsernames(): Chat {
        val affectedUserIds = lastMessage?.event?.affectedUserIds.orEmpty()
        val affectedUsernames = chatDb
            .chatParticipantDao
            .getUsernamesByUserIds(affectedUserIds)
            .map { it.username }

        return this.toDomain(affectedUsernames)
    }

    private suspend fun getUserId(): String {
        return sessionStorage
            .observeAuthenticatedUser()
            .firstOrNull()
            ?.user?.id ?: throw IllegalStateException("User is not logged in.")
    }

    override suspend fun fetchChats(): Result<List<Chat>, DataError.Remote> {
        return when (val result = chatService.getAllChats()) {
            is Result.Failure -> {
                result
            }
            is Result.Success -> {
                val chatsWithParticipants = result.data.map { (chat, affectedUserIds) ->
                    ChatWithParticipantsEntity(
                        chat = chat.toEntity(),
                        participants = chat.participants.filterNotNull().map { it.toEntity() },
                        lastMessage = chat.lastMessage?.toDatabaseView(affectedUserIds.orEmpty())
                    )
                }

                chatDb.chatDao.upsertChatsWithParticipantsAndCrossRefs(
                    localUserId = getUserId(),
                    chats = chatsWithParticipants,
                    participantDao = chatDb.chatParticipantDao,
                    crossRefDao = chatDb.chatParticipantCrossRefDao,
                    messageDao = chatDb.chatMessageDao
                )

                val chats = result.data.map { it.first }
                Result.Success(chats)
            }
        }
    }

    override fun getChatInfoById(chatId: String): Flow<ChatInfo> {
        return chatDb
            .chatDao
            .getChatInfoById(chatId)
            .filterNotNull()
            .map { it.toDomain(chatDb.chatParticipantDao) }
    }

    override suspend fun fetchChatById(chatId: String): EmptyResult<DataError.Remote> {
        return when (val result = chatService.getChatById(chatId)) {
            is Result.Failure -> {
                result
            }
            is Result.Success -> {
                val chatWithParticipants = result.data.let { (chat, affectedUserIds) ->
                    ChatWithParticipantsEntity(
                        chat = chat.toEntity(),
                        participants = chat.participants.filterNotNull().map { it.toEntity() },
                        lastMessage = chat.lastMessage?.toDatabaseView(affectedUserIds.orEmpty())
                    )
                }

                chatDb.chatDao.upsertChatWithParticipantsAndCrossRefs(
                    localUserId = getUserId(),
                    chat = chatWithParticipants,
                    participantDao = chatDb.chatParticipantDao,
                    crossRefDao = chatDb.chatParticipantCrossRefDao,
                    messageDao = chatDb.chatMessageDao
                )

                result.asEmptyResult()
            }
        }
    }

    override suspend fun getUsernameById(participantId: String): String? {
        return chatDb
            .chatParticipantDao
            .getUsernamesByUserIds(listOf(participantId))
            .firstOrNull()?.username
    }

    override suspend fun createChat(participantIds: List<String>): Result<Chat, DataError.Remote> {
        return when (val result = chatService.createChat(participantIds)) {
            is Result.Failure -> result
            is Result.Success -> {
                val chatWithParticipants = ChatWithParticipantsEntity(
                    chat = result.data.toEntity(),
                    participants = result.data.participants.map { it?.toEntity() },
                    lastMessage = null
                )
                chatDb.chatDao.upsertChatWithParticipantsAndCrossRefs(
                    localUserId = getUserId(),
                    chat = chatWithParticipants,
                    participantDao = chatDb.chatParticipantDao,
                    crossRefDao = chatDb.chatParticipantCrossRefDao,
                    messageDao = chatDb.chatMessageDao
                )

                result
            }
        }
    }

    override suspend fun leaveChat(chatId: String): EmptyResult<DataError.Remote> {
        return when (val result = chatService.leaveChat(chatId)) {
            is Result.Failure -> result
            is Result.Success -> {
                chatDb.chatDao.deleteChatById(chatId)
                result
            }
        }
    }

    override suspend fun addParticipants(
        chatId: String,
        participantIds: List<String>
    ): EmptyResult<DataError.Remote> {
        return when (val result = chatService.addParticipants(chatId, participantIds)) {
            is Result.Failure -> result
            is Result.Success -> {
                val chatWithParticipants = result.data.let { (chat, affectedUserIds) ->
                    ChatWithParticipantsEntity(
                        chat = chat.toEntity(),
                        participants = chat.participants.map { it?.toEntity() },
                        lastMessage = chat.lastMessage?.toDatabaseView(affectedUserIds.orEmpty())
                    )
                }

                chatDb.chatDao.upsertChatWithParticipantsAndCrossRefs(
                    localUserId = getUserId(),
                    chat = chatWithParticipants,
                    participantDao = chatDb.chatParticipantDao,
                    crossRefDao = chatDb.chatParticipantCrossRefDao,
                    messageDao = chatDb.chatMessageDao
                )

                result.asEmptyResult()
            }
        }
    }

    override suspend fun removeParticipant(
        chatId: String,
        participantId: String
    ): EmptyResult<DataError.Remote> {
        return when (val result = chatService.removeParticipant(chatId, participantId)) {
            is Result.Failure -> result
            is Result.Success -> {
                chatDb.chatParticipantCrossRefDao.deleteByChatAndParticipantId(chatId, participantId)
                result.asEmptyResult()
            }
        }
    }

    override fun getChatWithParticipants(chatId: String): Flow<Chat?> {
        return chatDb.chatDao.getChatWithParticipants(chatId).map {
            it?.toChatWithAffectedUsernames()
        }
    }

    override suspend fun removeAll() {
        chatDb.chatDao.deleteAllChats()
    }

    override suspend fun fetchProfileInfo() {
        val data = sessionStorage.observeAuthenticatedUser().first() ?: return
        when (val result = chatService.findParticipantByEmailOrUsername(null)) {
            is Result.Failure -> Unit
            is Result.Success -> {
                val profileImageUrl = result.data.profilePictureUrl
                sessionStorage.set(
                    data.copy(
                        user = data.user?.copy(
                            profileImageUrl = profileImageUrl
                        )
                    )
                )
            }
        }
    }
}