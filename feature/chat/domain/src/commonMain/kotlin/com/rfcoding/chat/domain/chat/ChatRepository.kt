package com.rfcoding.chat.domain.chat

import com.rfcoding.chat.domain.models.Chat
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun getAllChats(): Flow<List<Chat>>
    suspend fun fetchChats(): Result<List<Chat>, DataError.Remote>
    suspend fun getUsernameById(participantId: String): String?
}