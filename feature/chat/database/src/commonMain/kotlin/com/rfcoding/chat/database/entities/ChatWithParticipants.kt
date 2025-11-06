package com.rfcoding.chat.database.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ChatWithParticipants(
    @Embedded
    val chat: ChatEntity,
    @Relation(
        parentColumn = "chatId",
        entityColumn = "userId",
        associateBy = Junction(ChatParticipantCrossRef::class)
    )
    val participants: List<ChatParticipantEntity?>,
    @Relation(
        parentColumn = "creatorId",
        entityColumn = "userId"
    )
    val creator: ChatParticipantEntity?
)

data class ChatInfoEntity(
    @Embedded
    val chat: ChatEntity,
    @Relation(
        parentColumn = "chatId",
        entityColumn = "userId",
        associateBy = Junction(ChatParticipantCrossRef::class)
    )
    val participants: List<ChatParticipantEntity?>,
    @Relation(
        parentColumn = "creatorId",
        entityColumn = "userId"
    )
    val creator: ChatParticipantEntity?,
    @Relation(
        parentColumn = "chatId",
        entityColumn = "chatId",
        entity = ChatMessageEntity::class
    )
    val messagesWithSenders: List<MessageWithSenderEntity>
)