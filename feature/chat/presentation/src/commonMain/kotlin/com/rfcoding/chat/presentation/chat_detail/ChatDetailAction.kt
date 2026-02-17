package com.rfcoding.chat.presentation.chat_detail

import com.rfcoding.chat.presentation.model.MessageUi

sealed interface ChatDetailAction {
    data object OnSendMessageClick: ChatDetailAction
    data object OnScrollToTop: ChatDetailAction
    data class OnFirstVisibleIndexChanged(val index: Int): ChatDetailAction
    data class OnTopVisibleIndexChanged(val topVisibleIndex: Int): ChatDetailAction
    data class OnVisibleMessageIdsChanged(val messageIds: List<String>): ChatDetailAction
    data object OnStartAutoScrollToTop: ChatDetailAction
    data object OnHideBanner: ChatDetailAction

    // Chat list and detail is on the same back stack, that's why we still need to pass it here.
    data class OnSelectChat(val chatId: String?): ChatDetailAction

    // Message drop down menu actions
    data class OnMessageLongClick(val message: MessageUi.LocalUserMessage): ChatDetailAction
    data class OnDeleteMessageClick(val message: MessageUi.LocalUserMessage): ChatDetailAction
    data object OnDismissMessageMenu: ChatDetailAction

    // Retry sending message.
    data class OnRetryClick(val message: MessageUi.LocalUserMessage): ChatDetailAction
    data class OnImageClick(val value: String): ChatDetailAction
    data object OnCloseImageViewer: ChatDetailAction
    data object OnImageDownloadClick: ChatDetailAction
    data object OnBackClick: ChatDetailAction

    // Chat drop down menu actions
    data object OnChatOptionsClick: ChatDetailAction
    data object OnChatMembersClick: ChatDetailAction
    data object OnLeaveChatClick: ChatDetailAction
    data object OnDismissChatOptions: ChatDetailAction

    // Image file actions
    data object OnAttachImageClick: ChatDetailAction
    data class OnImagesSelected(val values: List<ByteArray>): ChatDetailAction
    data class OnRemoveImage(val id: String): ChatDetailAction

    // Voice record actions
    data object OnVoiceMessageClick: ChatDetailAction
    data object OnAudioPermissionGranted: ChatDetailAction
    data object OnConfirmVoiceMessageClick: ChatDetailAction
    data object OnCancelVoiceMessageClick: ChatDetailAction
    data class OnTogglePlayback(val message: MessageUi): ChatDetailAction
}