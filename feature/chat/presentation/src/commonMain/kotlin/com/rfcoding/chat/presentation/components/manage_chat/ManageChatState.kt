package com.rfcoding.chat.presentation.components.manage_chat

import androidx.compose.foundation.text.input.TextFieldState
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.domain.auth.AuthConstants
import com.rfcoding.core.presentation.util.UiText

data class ManageChatState(
    val queryTextFieldState: TextFieldState = TextFieldState(),
    val existingChatParticipants: List<ChatParticipantUi> = emptyList(),
    val selectedChatParticipants: List<ChatParticipantUi> = emptyList(),
    val isSearching: Boolean = false,
    val isCreatingChat: Boolean = false,
    val currentSearchResult: ChatParticipantUi? = null,
    val searchError: UiText? = null,
    val isCreator: Boolean = false
) {
    val canAddParticipant: Boolean
        get() = queryTextFieldState.text.length in AuthConstants.VALID_USERNAME_LENGTH_RANGE &&
                currentSearchResult != null &&
                !isSearching &&
                !isCreatingChat
}