package com.rfcoding.chat.domain.models

data class ChatMessageEvent(
    val affectedUserIds: List<String>,
    val type: ChatMessageEventType
)

enum class ChatMessageEventType {
    PARTICIPANTS_ADDED,
    PARTICIPANT_REMOVED_BY_CREATOR,
    PARTICIPANT_LEFT
}
