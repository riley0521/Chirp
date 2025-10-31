package com.rfcoding.chat.presentation.chat_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.core.domain.auth.SessionStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private var hasLoadedInitialData = false
    private lateinit var localUserId: String

    private val _state = MutableStateFlow(ChatListState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadUserId()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ChatListState()
        )

    private fun loadUserId() {
        viewModelScope.launch {
            localUserId = sessionStorage
                .observeAuthenticatedUser()
                .firstOrNull()
                ?.user?.id ?: throw IllegalStateException("User is not logged in.")
        }
    }

    fun onAction(action: ChatListAction) {
        when (action) {
            is ChatListAction.OnChatClick -> chatSelected(action.chatId)
            ChatListAction.OnDismissLogoutDialog -> {
                _state.update { it.copy(showLogoutConfirmation = false) }
            }
            ChatListAction.OnConfirmLogout -> confirmLogout()
            ChatListAction.OnLogoutClick -> {
                _state.update { it.copy(showLogoutConfirmation = true) }
            }
            ChatListAction.OnDismissUserMenu -> {
                _state.update { it.copy(isUserMenuOpen = false) }
            }
            ChatListAction.OnUserAvatarClick -> {
                _state.update { it.copy(isUserMenuOpen = true) }
            }

            ChatListAction.OnCreateChatClick -> Unit
        }
    }

    private fun chatSelected(chatId: String) {
        TODO()
    }

    private fun confirmLogout() {
        _state.update { it.copy(showLogoutConfirmation = false) }

        // TODO: Remove user session and navigate to login.
    }

}