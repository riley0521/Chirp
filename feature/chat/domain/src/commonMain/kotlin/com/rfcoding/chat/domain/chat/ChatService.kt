package com.rfcoding.chat.domain.chat

import com.rfcoding.chat.domain.models.ChatParticipant
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.Result

interface ChatService {

    suspend fun findParticipantByEmailOrUsername(
        query: String
    ): Result<ChatParticipant, DataError.Remote>
}