package com.rfcoding.chat.presentation.chat_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.chat.domain.chat.ChatRepository
import com.rfcoding.chat.domain.chat.ChatService
import com.rfcoding.chat.presentation.mappers.toUi
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.domain.auth.AuthenticatedUser
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.domain.util.Result
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ChatListViewModel(
    private val chatRepository: ChatRepository,
    private val chatService: ChatService,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(ChatListState())
    val state = combine(
        _state,
        chatRepository.getAllChats().debounce(500L),
        sessionStorage.observeAuthenticatedUser()
    ) { curState, chats, authInfo ->
        if (authInfo == null || authInfo.user == null) {
            return@combine ChatListState()
        }

        curState.copy(
            chats = chats.map { chat ->
                val lastMessageUsername = chat.lastMessage?.senderId?.let {
                    chatRepository.getUsernameById(it)
                }

                chat.toUi(
                    localUserId = authInfo.user?.id ?: return@combine ChatListState(),
                    lastMessageUsername = lastMessageUsername,
                    affectedUsernamesForEvent = chat.lastMessage?.event?.affectedUsernames.orEmpty()
                )
            },
            localParticipant = ChatParticipantUi(
                id = authInfo.user!!.id,
                username = authInfo.user!!.username,
                initial = authInfo.user!!.username.take(2).uppercase(),
                imageUrl = authInfo.user!!.profileImageUrl
            )
        )
    }.onStart {
        if (!hasLoadedInitialData) {
            viewModelScope.launch {
                loadLocalParticipant().join()
                fetchChats().join()
            }
            hasLoadedInitialData = true
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000L),
        ChatListState()
    )

    private fun loadLocalParticipant(): Job {
        return viewModelScope.launch {
            val data = sessionStorage
                .observeAuthenticatedUser()
                .firstOrNull() ?: throw IllegalStateException("User is not logged in.")

            // Get user data and assign id to localUserId
            val user = data.user ?: throw IllegalStateException("User is not logged in.")

            // Fetch the profileImageUrl from chat participant endpoint, since auth endpoints don't support it.
            fetchProfileImageIfFirstLogin(data)
        }
    }

    private suspend fun fetchProfileImageIfFirstLogin(data: AuthenticatedUser) {
        if (!data.isFirstLogin) {
            return
        }

        when (val result = chatService.findParticipantByEmailOrUsername(null)) {
            is Result.Failure -> {
                sessionStorage.set(data.copy(isFirstLogin = false))
            }

            is Result.Success -> {
                val profileImageUrl = result.data.profilePictureUrl
                sessionStorage.set(
                    data.copy(
                        isFirstLogin = false,
                        user = data.user!!.copy(
                            profileImageUrl = profileImageUrl
                        )
                    )
                )
            }
        }
    }

    fun onAction(action: ChatListAction) {
        when (action) {
            is ChatListAction.OnSelectChat -> chatSelected(action.chatId)
            ChatListAction.OnDismissLogoutDialog -> {
                _state.update { it.copy(showLogoutConfirmation = false) }
            }

            ChatListAction.OnLogoutClick -> {
                _state.update { it.copy(showLogoutConfirmation = true, isUserMenuOpen = false) }
            }

            ChatListAction.OnDismissUserMenu -> {
                _state.update { it.copy(isUserMenuOpen = false) }
            }

            ChatListAction.OnUserAvatarClick -> {
                _state.update { it.copy(isUserMenuOpen = true) }
            }

            ChatListAction.OnProfileSettingsClick -> {
                _state.update { it.copy(isUserMenuOpen = false) }
            }
            ChatListAction.OnCreateChatClick -> Unit
            ChatListAction.OnConfirmLogout -> Unit
        }
    }

    private fun fetchChats(): Job {
        return viewModelScope.launch {
            _state.update { it.copy(isLoadingChats = true) }

            chatRepository.fetchChats()
            delay(1_000L)
            _state.update { it.copy(isLoadingChats = false) }
        }
    }

    private fun chatSelected(chatId: String?) {
        _state.update {
            it.copy(
                selectedChatId = chatId
            )
        }
    }
}