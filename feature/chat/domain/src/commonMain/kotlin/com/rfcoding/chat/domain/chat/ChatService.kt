package com.rfcoding.chat.domain.chat

import com.rfcoding.chat.domain.models.Chat
import com.rfcoding.chat.domain.models.ChatParticipant
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
import com.rfcoding.core.domain.util.Result

typealias ChatWithAffectedUserIds = Pair<Chat, List<String>?>

interface ChatService {

    suspend fun findParticipantByEmailOrUsername(
        query: String?
    ): Result<ChatParticipant, DataError.Remote>

    suspend fun createChat(
        participantIds: List<String>
    ): Result<Chat, DataError.Remote>

    suspend fun fetchChatIds(): Result<List<String>, DataError.Remote>

    /**
     * @param before It is the last activity time of the oldest chat.
     */
    suspend fun fetchChats(before: String? = null): Result<List<ChatWithAffectedUserIds>, DataError.Remote>

    suspend fun getChatById(chatId: String): Result<ChatWithAffectedUserIds, DataError.Remote>

    suspend fun leaveChat(chatId: String): EmptyResult<DataError.Remote>

    suspend fun addParticipants(
        chatId: String,
        participantIds: List<String>
    ): Result<ChatWithAffectedUserIds, DataError.Remote>

    suspend fun removeParticipant(
        chatId: String,
        otherUserId: String
    ): Result<Pair<Chat?, List<String>?>, DataError.Remote>

    suspend fun uploadProfilePicture(
        mimeType: String,
        imageBytes: ByteArray
    ): Result<String, DataError.Remote>

    suspend fun deleteProfilePicture(): EmptyResult<DataError.Remote>
}