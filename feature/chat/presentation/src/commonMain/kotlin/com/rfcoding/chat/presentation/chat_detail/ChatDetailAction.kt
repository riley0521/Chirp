package com.rfcoding.chat.presentation.chat_detail

import com.rfcoding.chat.presentation.model.MessageUi

sealed interface ChatDetailAction {
    data object OnSendMessageClick: ChatDetailAction
    data object OnScrollToTop: ChatDetailAction
    data class OnScroll(val isNearBottom: Boolean): ChatDetailAction

    // Chat list and detail is on the same back stack, that's why we still need to pass it here.
    data class OnSelectChat(val chatId: String?): ChatDetailAction

    // Message drop down menu actions
    data class OnMessageLongClick(val message: MessageUi.LocalUserMessage): ChatDetailAction
    data class OnDeleteMessageClick(val message: MessageUi.LocalUserMessage): ChatDetailAction
    data object OnDismissMessageMenu: ChatDetailAction

    // Retry sending message.
    data class OnRetryClick(val message: MessageUi.LocalUserMessage): ChatDetailAction
    data class OnImageClick(val value: String): ChatDetailAction
    data object OnBackClick: ChatDetailAction

    // Chat drop down menu actions
    data object OnChatOptionsClick: ChatDetailAction
    data object OnChatMembersClick: ChatDetailAction
    data object OnLeaveChatClick: ChatDetailAction
    data object OnDismissChatOptions: ChatDetailAction
}