package com.rfcoding.chat.data.message

import com.rfcoding.chat.data.chat.dto.ChatMessageDto
import com.rfcoding.chat.data.chat.dto.MediaUrlDto
import com.rfcoding.chat.data.mappers.toDomain
import com.rfcoding.chat.domain.message.ChatMessageService
import com.rfcoding.chat.domain.models.ChatMessage
import com.rfcoding.core.data.networking.createRoute
import com.rfcoding.core.data.networking.delete
import com.rfcoding.core.data.networking.get
import com.rfcoding.core.data.networking.safeCall
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
import com.rfcoding.core.domain.util.Result
import com.rfcoding.core.domain.util.map
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class KtorChatMessageService(
    private val httpClient: HttpClient
): ChatMessageService {

    override suspend fun fetchMessages(
        chatId: String,
        before: String?
    ): Result<List<Pair<ChatMessage, List<String>?>>, DataError.Remote> {
        return httpClient.get<List<ChatMessageDto>>(
            route = "/chats/$chatId/messages",
            queryParams = buildMap {
                if (before != null) {
                    this["before"] = before
                }
            }
        ).map { messages ->
            messages.map {
                it.toDomain() to it.event?.affectedUserIds
            }
        }
    }

    override suspend fun fetchMessage(messageId: String): Result<Pair<ChatMessage, List<String>?>, DataError.Remote> {
        return httpClient.get<ChatMessageDto>(
            route = "/messages/$messageId"
        ).map { it.toDomain() to it.event?.affectedUserIds }
    }

    override suspend fun deleteMessage(messageId: String): EmptyResult<DataError.Remote> {
        return httpClient.delete(
            route = "/messages/$messageId"
        )
    }

    override suspend fun uploadFile(chatId: String, bytes: ByteArray): Result<String, DataError.Remote> {
        return safeCall<MediaUrlDto> {
            val mimeType = "image/*"

            httpClient.post {
                url(createRoute("/messages/upload-media"))
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("chatId", chatId)
                            append(
                                "file",
                                bytes,
                                Headers.build {
                                    append(HttpHeaders.ContentType, mimeType)
                                    append(HttpHeaders.ContentDisposition, "filename=\"chirp_photo.jpg\"")
                                }
                            )
                        }
                    )
                )
            }
        }.map { it.newUrl }
    }
}