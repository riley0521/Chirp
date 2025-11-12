package com.rfcoding.chat.data.chat

import com.rfcoding.chat.database.ChirpChatDatabase
import com.rfcoding.chat.database.entities.ChatWithParticipantsEntity
import com.rfcoding.chat.database.mapper.toDatabaseView
import com.rfcoding.chat.database.mapper.toDomain
import com.rfcoding.chat.database.mapper.toEntity
import com.rfcoding.chat.domain.chat.ChatRepository
import com.rfcoding.chat.domain.chat.ChatService
import com.rfcoding.chat.domain.models.Chat
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class OfflineFirstChatRepository(
    private val chatService: ChatService,
    private val chatDb: ChirpChatDatabase,
    private val sessionStorage: SessionStorage
): ChatRepository {

    override fun getAllChats(): Flow<List<Chat>> {
        return chatDb.chatDao.getChatsWithParticipants().map { chatsWithParticipants ->
            chatsWithParticipants.map { item ->
                val affectedUserIds = item.lastMessage?.event?.affectedUserIds.orEmpty()
                val affectedUsernames = chatDb
                    .chatParticipantDao
                    .getUsernamesByUserIds(affectedUserIds)
                    .map { it.username }

                item.toDomain(affectedUsernames)
            }
        }
    }

    override suspend fun fetchChats(): Result<List<Chat>, DataError.Remote> {
        return when (val result = chatService.getAllChats()) {
            is Result.Failure -> {
                result
            }
            is Result.Success -> {
                val userId = sessionStorage
                    .observeAuthenticatedUser()
                    .firstOrNull()
                    ?.user?.id ?: return Result.Failure(DataError.Remote.UNAUTHORIZED)

                val chatsWithParticipants = result.data.map { (chat, affectedUserIds) ->
                    ChatWithParticipantsEntity(
                        chat = chat.toEntity(),
                        participants = chat.participants.filterNotNull().map { it.toEntity() },
                        lastMessage = chat.lastMessage?.toDatabaseView(affectedUserIds.orEmpty())
                    )
                }

                chatDb.chatDao.upsertChatsWithParticipantsAndCrossRefs(
                    localUserId = userId,
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

    override suspend fun getUsernameById(participantId: String): String? {
        return chatDb
            .chatParticipantDao
            .getUsernamesByUserIds(listOf(participantId))
            .firstOrNull()?.username
    }
}