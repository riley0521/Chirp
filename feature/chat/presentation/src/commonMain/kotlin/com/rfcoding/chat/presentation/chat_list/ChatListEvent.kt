package com.rfcoding.chat.presentation.chat_list

import com.rfcoding.core.presentation.util.UiText

sealed interface ChatListEvent {
    data class Error(val error: UiText): ChatListEvent
    data object OnSuccessfulLogout: ChatListEvent
}