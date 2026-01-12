package com.rfcoding.chat.presentation.chat_detail

sealed interface ChatDetailEvent {
    data object LeaveChatSuccessful: ChatDetailEvent
    data object OnNewMessage: ChatDetailEvent
    data object RequestAudioPermission: ChatDetailEvent
}