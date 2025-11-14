package com.rfcoding.chat.presentation.manage_chat

sealed interface ManageChatEvent {
    data object OnChatMembersModified: ManageChatEvent
}