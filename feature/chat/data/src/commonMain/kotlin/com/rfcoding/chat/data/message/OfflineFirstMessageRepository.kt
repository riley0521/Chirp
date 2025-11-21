package com.rfcoding.chat.data.message

import com.rfcoding.chat.database.ChirpChatDatabase
import com.rfcoding.chat.domain.message.MessageRepository
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.core.data.database.safeDatabaseUpdate
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
import kotlin.time.Clock

class OfflineFirstMessageRepository(
    private val chatDb: ChirpChatDatabase
): MessageRepository {

    override suspend fun updateMessageDeliveryStatus(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate {
            chatDb.chatMessageDao.updateDeliveryStatus(
                id = messageId,
                deliveryStatus = status,
                deliveredAt = Clock.System.now()
            )
        }
    }
}