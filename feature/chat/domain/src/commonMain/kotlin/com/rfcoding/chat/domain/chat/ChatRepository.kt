package com.rfcoding.chat.domain.chat

import com.rfcoding.chat.domain.models.Chat
import com.rfcoding.chat.domain.models.ChatInfo
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
import com.rfcoding.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun getAllChats(): Flow<List<Chat>>
    suspend fun fetchChatIds(): EmptyResult<DataError.Remote>
    suspend fun fetchChats(before: String? = null): Result<List<Chat>, DataError.Remote>
    fun getChatInfoById(chatId: String): Flow<ChatInfo>
    suspend fun fetchChatById(chatId: String): EmptyResult<DataError.Remote>
    suspend fun getUsernameById(participantId: String): String?
    suspend fun createChat(participantIds: List<String>): Result<Chat, DataError.Remote>
    suspend fun leaveChat(chatId: String): EmptyResult<DataError.Remote>
    suspend fun addParticipants(chatId: String, participantIds: List<String>): EmptyResult<DataError.Remote>

    /**
     * @return True if the chat was deleted, and false if the chat is still existing after removing participant.
     */
    suspend fun removeParticipant(chatId: String, participantId: String): Result<Boolean, DataError.Remote>
    fun getChatWithParticipants(chatId: String): Flow<Chat?>
    suspend fun removeAll()
    suspend fun fetchProfileInfo()
}