package com.rfcoding.chat.presentation.create_chat

sealed interface CreateChatEvent {
    data object Success: CreateChatEvent
}