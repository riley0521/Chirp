package com.rfcoding.chat.data.chat

import com.rfcoding.chat.data.chat.dto.ChatDto
import com.rfcoding.chat.data.chat.dto.ChatParticipantDto
import com.rfcoding.chat.data.chat.dto.CreateChatRequest
import com.rfcoding.chat.data.mappers.toDomain
import com.rfcoding.chat.domain.chat.ChatService
import com.rfcoding.chat.domain.chat.ChatWithAffectedUserIds
import com.rfcoding.chat.domain.models.Chat
import com.rfcoding.chat.domain.models.ChatParticipant
import com.rfcoding.core.data.networking.delete
import com.rfcoding.core.data.networking.get
import com.rfcoding.core.data.networking.post
import com.rfcoding.core.data.networking.put
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
import com.rfcoding.core.domain.util.Result
import com.rfcoding.core.domain.util.asEmptyResult
import com.rfcoding.core.domain.util.map
import io.ktor.client.HttpClient

class KtorChatService(
    private val httpClient: HttpClient
): ChatService {

    override suspend fun findParticipantByEmailOrUsername(
        query: String?
    ): Result<ChatParticipant, DataError.Remote> {
        return httpClient.get<ChatParticipantDto>(
            route = "/users",
            queryParams = query?.let {
                mapOf(
                    "query" to query
                )
            } ?: mapOf()
        ).map {
            it.toDomain()
        }
    }

    override suspend fun createChat(
        participantIds: List<String>
    ): Result<Chat, DataError.Remote> {
        return httpClient.post<CreateChatRequest, ChatDto>(
            route = "/chats",
            body = CreateChatRequest(
                otherUserIds = participantIds.toSet()
            )
        ).map { it.toDomain() }
    }

    private fun ChatDto.toChatWithAffectedUserIds(): ChatWithAffectedUserIds {
        return this.toDomain() to this.lastMessage?.event?.affectedUserIds
    }

    override suspend fun getAllChats(): Result<List<ChatWithAffectedUserIds>, DataError.Remote> {
        return httpClient.get<List<ChatDto>>(
            route = "/chats"
        ).map { chats ->
            chats.map { it.toChatWithAffectedUserIds() }
        }
    }

    override suspend fun getChatById(chatId: String): Result<ChatWithAffectedUserIds, DataError.Remote> {
        return httpClient.get<ChatDto>(
            route = "/chats/$chatId"
        ).map { it.toChatWithAffectedUserIds() }
    }

    override suspend fun leaveChat(chatId: String): EmptyResult<DataError.Remote> {
        return httpClient.delete<Unit>(
            route = "/chats/$chatId/leave"
        ).asEmptyResult()
    }

    override suspend fun addParticipants(
        chatId: String,
        participantIds: List<String>
    ): Result<ChatWithAffectedUserIds, DataError.Remote> {
        return httpClient.put<CreateChatRequest, ChatDto>(
            route = "/chats/$chatId/add",
            body = CreateChatRequest(
                otherUserIds = participantIds.toSet()
            )
        ).map { it.toChatWithAffectedUserIds() }
    }
}