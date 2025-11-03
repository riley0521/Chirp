package com.rfcoding.chat.data.chat

import com.rfcoding.chat.data.chat.dto.ChatDto
import com.rfcoding.chat.data.chat.dto.ChatParticipantDto
import com.rfcoding.chat.data.chat.dto.CreateChatRequest
import com.rfcoding.chat.data.mappers.toDomain
import com.rfcoding.chat.domain.chat.ChatService
import com.rfcoding.chat.domain.models.Chat
import com.rfcoding.chat.domain.models.ChatParticipant
import com.rfcoding.core.data.networking.get
import com.rfcoding.core.data.networking.post
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.Result
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
}