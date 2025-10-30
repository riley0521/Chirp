package com.rfcoding.chat.presentation.create_chat

import com.rfcoding.chat.domain.models.Chat

sealed interface CreateChatEvent {
    data class OnChatCreated(val chat: Chat): CreateChatEvent
}