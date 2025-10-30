package com.rfcoding.chat.presentation.create_chat

import androidx.compose.foundation.text.input.TextFieldState
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.domain.auth.AuthConstants
import com.rfcoding.core.presentation.util.UiText

data class CreateChatState(
    val queryTextFieldState: TextFieldState = TextFieldState(),
    val selectedChatParticipants: List<ChatParticipantUi> = emptyList(),
    val isAddingParticipant: Boolean = false,
    val isLoadingParticipants: Boolean = false,
    val isCreatingChat: Boolean = false,
    val currentSearchResult: ChatParticipantUi? = null,
    val searchError: UiText? = null
) {
    val canAddParticipant: Boolean
        get() = queryTextFieldState.text.length in AuthConstants.VALID_USERNAME_LENGTH_RANGE &&
                /**
                 * I think this is only used for managing chat, not when creating it.
                 * When creating chat, you can just add all desired participants to selectedChatParticipants directly.
                 */
                !isAddingParticipant &&
                /**
                 * I think this one is for loading the added participants when we're editing the chat.
                 */
                !isLoadingParticipants &&
                !isCreatingChat
}