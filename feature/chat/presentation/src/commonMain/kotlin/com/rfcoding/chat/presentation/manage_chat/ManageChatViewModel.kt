package com.rfcoding.chat.presentation.manage_chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.chat.domain.chat.ChatRepository
import com.rfcoding.chat.presentation.components.manage_chat.ManageChatAction
import com.rfcoding.chat.presentation.components.manage_chat.ManageChatState
import com.rfcoding.chat.presentation.mappers.toUi
import com.rfcoding.core.domain.auth.SessionStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ManageChatViewModel(
    private val chatRepository: ChatRepository,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _chatId = MutableStateFlow<String?>(null)
    private val _chatInfo = _chatId
        .flatMapLatest { chatId ->
            if (chatId != null) {
                chatRepository.getChatInfoById(chatId)
            } else emptyFlow()
        }

    private val _state = MutableStateFlow(ManageChatState())
    val state = combine(
        _state,
        _chatInfo,
        sessionStorage.observeAuthenticatedUser()
    ) { curState, chatInfo, authInfo ->
        if (authInfo == null) {
            return@combine ManageChatState()
        }
        val localUserId = authInfo.user?.id ?: return@combine ManageChatState()
        val isCreator = chatInfo.chat.creator?.userId == localUserId

        curState.copy(
            isCreator = isCreator,
            existingChatParticipants = chatInfo.chat.participants.mapNotNull { it?.toUi() }
        )
    }.onStart {
        if (!hasLoadedInitialData) {
            // Load initial data here.
            hasLoadedInitialData = true
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000L),
        ManageChatState()
    )

    private val eventChannel = Channel<ManageChatEvent>()
    val events = eventChannel.receiveAsFlow()


    fun onAction(action: ManageChatAction) {
        when (action) {
            ManageChatAction.OnAddClick -> addMemberIfNotExists()
            is ManageChatAction.OnChatSelect -> {
                _chatId.update { action.chatId }
            }

            ManageChatAction.OnPrimaryButtonClick -> modifyChatMembers()
            ManageChatAction.OnDismissDialog -> Unit
        }
    }

    private fun addMemberIfNotExists() {
        val searchResult = state.value.currentSearchResult
        if (searchResult == null) {
            return
        }

        val isExisting = state.value.existingChatParticipants.any {
            it.id == searchResult.id
        }
        if (isExisting) {
            return
        }

        _state.update {
            it.copy(
                selectedChatParticipants = it.selectedChatParticipants + searchResult
            )
        }
    }

    private fun modifyChatMembers() {
        if (state.value.selectedChatParticipants.isEmpty()) {
            return
        }

        viewModelScope.launch {
            delay(3_000L)
            eventChannel.send(ManageChatEvent.OnChatMembersModified)
        }
    }
}