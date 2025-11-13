package com.rfcoding.chat.presentation.chat_detail

import androidx.compose.foundation.text.input.TextFieldState
import com.rfcoding.chat.domain.models.ConnectionState
import com.rfcoding.chat.presentation.model.ChatUi
import com.rfcoding.chat.presentation.model.MessageUi
import com.rfcoding.core.presentation.util.UiText

data class ChatDetailState(
    val chatUi: ChatUi? = null,
    val isLoading: Boolean = false,
    val messages: List<MessageUi> = emptyList(),
    val error: UiText? = null,
    val messageTextFieldState: TextFieldState = TextFieldState(),
    // val canSendMessage: Boolean = false,
    val isPaginationLoading: Boolean = false,
    val paginationError: UiText? = null,
    val endReached: Boolean = false,
    val bannerState: BannerState = BannerState(),
    val isChatOptionsOpen: Boolean = false,
    val isNearBottom: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED
) {
    val canSendMessage: Boolean
        get() {
            return messageTextFieldState.text.isNotBlank() && true
                    // connectionState == ConnectionState.CONNECTED
        }
}

data class BannerState(
    val formattedDate: String? = null,
    val isVisible: Boolean = false
)