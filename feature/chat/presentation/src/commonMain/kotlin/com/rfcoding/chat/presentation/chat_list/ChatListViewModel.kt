package com.rfcoding.chat.presentation.chat_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.chat.domain.chat.ChatConnectionClient
import com.rfcoding.chat.domain.chat.ChatRepository
import com.rfcoding.chat.domain.models.ConnectionState
import com.rfcoding.chat.domain.notification.DeviceTokenService
import com.rfcoding.chat.domain.notification.PushNotificationService
import com.rfcoding.chat.presentation.mappers.toUi
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.domain.auth.AuthService
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.domain.util.Result
import com.rfcoding.core.presentation.util.toUiText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ChatListViewModel(
    private val chatRepository: ChatRepository,
    private val authService: AuthService,
    private val pushNotificationService: PushNotificationService,
    private val deviceTokenService: DeviceTokenService,
    private val sessionStorage: SessionStorage,
    private val connector: ChatConnectionClient
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
                chatRepository.fetchProfileInfo()
                fetchChats()
            }
            observeConnectionState()
            hasLoadedInitialData = true
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000L),
        ChatListState()
    )

    private val eventChannel = Channel<ChatListEvent>()
    val events = eventChannel.receiveAsFlow()

    private var fetchChatsJob: Job? = null

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
            ChatListAction.OnConfirmLogout -> confirmLogout()
            ChatListAction.OnCreateChatClick -> Unit
        }
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            connector.connectionState.collect { connectionState ->
                if (connectionState == ConnectionState.CONNECTED) {
                    fetchChats()
                }
            }
        }
    }

    private fun confirmLogout() {
        viewModelScope.launch {
            val refreshToken = sessionStorage
                .observeAuthenticatedUser()
                .first()
                ?.refreshToken

            if (refreshToken == null) {
                return@launch
            }

            _state.update { it.copy(isLoggingOut = true) }

            // Unregister FCM token first while authenticated. Before logging out.
            pushNotificationService.observeDeviceToken().first()?.let { token ->
                val unregisterResult = deviceTokenService.unregisterToken(token)
                if (unregisterResult is Result.Failure) {
                    eventChannel.send(ChatListEvent.Error(unregisterResult.toUiText()))
                    return@launch
                }
            }
            // End of logic

            when (val result = authService.logout(refreshToken)) {
                is Result.Failure -> {
                    eventChannel.send(ChatListEvent.Error(result.toUiText()))
                }
                is Result.Success -> {
                    sessionStorage.set(null)
                    chatRepository.removeAll()

                    _state.update { it.copy(showLogoutConfirmation = false) }
                    eventChannel.send(ChatListEvent.OnSuccessfulLogout)
                }
            }

            _state.update { it.copy(isLoggingOut = false) }
        }
    }

    private suspend fun fetchChats() {
        if (fetchChatsJob?.isActive == true) {
            return
        }

        _state.update { it.copy(isLoadingChats = true) }

        fetchChatsJob = viewModelScope.launch { chatRepository.fetchChats() }.also {
            it.join()
        }

        // Add artificial delay to at least show the loading for a while :D
        delay(1_000L)
        _state.update { it.copy(isLoadingChats = false) }
    }

    private fun chatSelected(chatId: String?) {
        _state.update {
            it.copy(
                selectedChatId = chatId
            )
        }
    }
}