package com.rfcoding.chat.presentation.components.manage_chat

sealed interface ManageChatAction {
    data object OnAddClick: ManageChatAction
    data object OnDismissDialog: ManageChatAction
    data object OnCreateChatClick: ManageChatAction
    data class OnChatSelect(val chatId: String): ManageChatAction
}