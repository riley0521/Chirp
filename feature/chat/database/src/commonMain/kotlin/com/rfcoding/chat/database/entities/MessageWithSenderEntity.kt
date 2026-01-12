package com.rfcoding.chat.database.entities

import androidx.room.Embedded
import androidx.room.Relation

data class MessageWithSenderEntity(
    @Embedded
    val message: ChatMessageEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "messageId"
    )
    val medias: List<ChatMediaEntity>,
    @Relation(
        parentColumn = "senderId",
        entityColumn = "userId"
    )
    val sender: ChatParticipantEntity?
)
