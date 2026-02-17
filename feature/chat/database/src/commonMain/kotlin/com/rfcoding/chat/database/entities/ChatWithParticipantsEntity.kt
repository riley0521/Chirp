package com.rfcoding.chat.database.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.rfcoding.chat.database.view.LastMessageView

/**
 * @param creator no need to pass this when instantiating this object.
 */
data class ChatWithParticipantsEntity(
    @Embedded
    val chat: ChatEntity,
    @Relation(
        parentColumn = "chatId",
        entityColumn = "userId",
        associateBy = Junction(ChatParticipantCrossRef::class)
    )
    val participants: List<ChatParticipantEntity?>,
    @Relation(
        parentColumn = "chatId",
        entityColumn = "chatId",
        entity = LastMessageView::class
    )
    val lastMessage: LastMessageView?,
    @Relation(
        parentColumn = "creatorId",
        entityColumn = "userId"
    )
    val creator: ChatParticipantEntity? = null,
    @Relation(
        parentColumn = "chatId",
        entityColumn = "chatId"
    )
    val unseenMessages: List<UnseenMessageEntity> = emptyList()
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