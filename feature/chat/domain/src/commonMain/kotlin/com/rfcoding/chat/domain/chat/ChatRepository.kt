package com.rfcoding.chat.domain.chat

import com.rfcoding.chat.domain.models.Chat
import com.rfcoding.chat.domain.models.ChatInfo
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
import com.rfcoding.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun getAllChats(): Flow<List<Chat>>
    suspend fun fetchChats(): Result<List<Chat>, DataError.Remote>
    fun getChatInfoById(chatId: String): Flow<ChatInfo>
    suspend fun fetchChatById(chatId: String): EmptyResult<DataError.Remote>
    suspend fun getUsernameById(participantId: String): String?
    suspend fun createChat(participantIds: List<String>): Result<Chat, DataError.Remote>
    suspend fun leaveChat(chatId: String): EmptyResult<DataError.Remote>
    suspend fun addParticipants(chatId: String, participantIds: List<String>): EmptyResult<DataError.Remote>
    suspend fun removeParticipant(chatId: String, participantId: String): EmptyResult<DataError.Remote>
    fun getChatWithParticipants(chatId: String): Flow<Chat?>
    suspend fun removeAll()
    suspend fun fetchProfileInfo()
}