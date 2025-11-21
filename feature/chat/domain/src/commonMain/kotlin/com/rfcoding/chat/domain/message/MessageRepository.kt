package com.rfcoding.chat.domain.message

import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult

interface MessageRepository {

    suspend fun updateMessageDeliveryStatus(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ): EmptyResult<DataError.Local>
}