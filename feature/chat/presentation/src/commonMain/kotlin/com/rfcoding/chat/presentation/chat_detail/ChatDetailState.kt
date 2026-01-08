package com.rfcoding.chat.presentation.chat_detail

import androidx.compose.foundation.text.input.TextFieldState
import com.rfcoding.chat.domain.models.ConnectionState
import com.rfcoding.chat.presentation.model.ChatUi
import com.rfcoding.chat.presentation.model.MessageUi
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.presentation.util.UiText

data class ChatDetailState(
    val chatUi: ChatUi? = null,
    val otherUsersTyping: List<ChatParticipantUi> = emptyList(),
    val isLoading: Boolean = false,
    val messages: List<MessageUi> = emptyList(),
    val error: UiText? = null,
    val messageTextFieldState: TextFieldState = TextFieldState(),
    val isPaginationLoading: Boolean = false,
    val paginationError: UiText? = null,
    val endReached: Boolean = false,
    val messageWithOpenMenu: MessageUi.LocalUserMessage? = null,
    val bannerState: BannerState = BannerState(),
    val isChatOptionsOpen: Boolean = false,
    val isNearBottom: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED
) {
    val canSendMessage: Boolean
        get() {
            return messageTextFieldState.text.isNotBlank() &&
                    connectionState == ConnectionState.CONNECTED &&
                    !isLoading
        }

    val typingUsers: String get() = otherUsersTyping
        .take(3)
        .joinToString(", ") { it.username }
}

data class BannerState(
    val formattedDate: UiText? = null,
    val isVisible: Boolean = false
)