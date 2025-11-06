package com.rfcoding.chat.presentation.chat_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class ChatDetailViewModel : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(ChatDetailState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                /** Load initial data here **/
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ChatDetailState()
        )

    fun onAction(action: ChatDetailAction) {
        when (action) {
            ChatDetailAction.OnBackClick -> TODO()
            ChatDetailAction.OnChatMembersClick -> TODO()
            ChatDetailAction.OnChatOptionsClick -> TODO()
            is ChatDetailAction.OnDeleteMessageClick -> TODO()
            ChatDetailAction.OnDismissChatOptions -> TODO()
            ChatDetailAction.OnDismissMessageMenu -> TODO()
            is ChatDetailAction.OnImageClick -> TODO()
            ChatDetailAction.OnLeaveChatClick -> TODO()
            is ChatDetailAction.OnMessageLongClick -> TODO()
            is ChatDetailAction.OnRetryClick -> TODO()
            ChatDetailAction.OnScrollToTop -> TODO()
            is ChatDetailAction.OnSelectChat -> TODO()
            ChatDetailAction.OnSendMessageClick -> TODO()
        }
    }

}