package com.rfcoding.chat.presentation.components.manage_chat

import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi

sealed interface ManageChatAction {
    data object OnAddClick: ManageChatAction
    data object OnDismissDialog: ManageChatAction
    data object OnPrimaryButtonClick: ManageChatAction
    data class OnChatSelect(val chatId: String?): ManageChatAction
    data class OnRemoveParticipantClick(val participant: ChatParticipantUi): ManageChatAction
}