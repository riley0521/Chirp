package com.rfcoding.chat.presentation.manage_chat

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.error_participant_already_in_chat
import com.rfcoding.chat.domain.chat.ChatRepository
import com.rfcoding.chat.domain.chat.ChatService
import com.rfcoding.chat.presentation.components.manage_chat.ManageChatAction
import com.rfcoding.chat.presentation.components.manage_chat.ManageChatState
import com.rfcoding.chat.presentation.mappers.toUi
import com.rfcoding.core.domain.auth.AuthConstants
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.domain.util.Result
import com.rfcoding.core.presentation.util.UiText
import com.rfcoding.core.presentation.util.toUiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ManageChatViewModel(
    private val chatRepository: ChatRepository,
    private val chatService: ChatService,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _chatId = MutableStateFlow<String?>(null)
    private val _chatInfo = _chatId
        .flatMapLatest { chatId ->
            if (chatId != null) {
                chatRepository.getChatWithParticipants(chatId)
            } else emptyFlow()
        }

    private val _state = MutableStateFlow(ManageChatState())
    val state = combine(
        _state,
        _chatInfo,
        sessionStorage.observeAuthenticatedUser()
    ) { curState, chatInfo, authInfo ->
        if (authInfo == null || chatInfo == null) {
            return@combine ManageChatState()
        }
        val localUserId = authInfo.user?.id ?: return@combine ManageChatState()
        val isCreator = chatInfo.creator?.userId == localUserId

        curState.copy(
            isCreator = isCreator,
            existingChatParticipants = chatInfo.participants.mapNotNull { it?.toUi() }
        )
    }.onStart {
        if (!hasLoadedInitialData) {
            queryFlow.launchIn(viewModelScope)
            hasLoadedInitialData = true
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000L),
        ManageChatState()
    )

    private val eventChannel = Channel<ManageChatEvent>()
    val events = eventChannel.receiveAsFlow()

    private val queryFlow = snapshotFlow { _state.value.queryTextFieldState.text.toString() }
        .debounce(500L)
        .onEach { query ->
            findByEmailOrUsername(query)
        }

    private fun findByEmailOrUsername(query: String) {
        if (query.isBlank() || query.length !in AuthConstants.VALID_USERNAME_LENGTH_RANGE) {
            _state.update {
                it.copy(
                    currentSearchResult = null,
                    searchError = null
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSearching = true) }

            when (val result = chatService.findParticipantByEmailOrUsername(query)) {
                is Result.Failure -> {
                    _state.update { it.copy(searchError = result.toUiText()) }
                }
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            currentSearchResult = result.data.toUi(),
                            searchError = null
                        )
                    }
                }
            }

            _state.update { it.copy(isSearching = false) }
        }
    }

    fun onAction(action: ManageChatAction) {
        when (action) {
            ManageChatAction.OnAddClick -> addParticipantIfNotExists()
            is ManageChatAction.OnChatSelect -> {
                _chatId.update { action.chatId }
            }

            ManageChatAction.OnPrimaryButtonClick -> addParticipantsToChat()
            ManageChatAction.OnDismissDialog -> Unit
        }
    }

    private fun addParticipantIfNotExists() {
        val searchResult = state.value.currentSearchResult
        if (searchResult == null) {
            return
        }

        val isExisting = state.value.existingChatParticipants.any {
            it.id == searchResult.id
        } || state.value.selectedChatParticipants.any {
            it.id == searchResult.id
        }
        if (isExisting) {
            _state.update {
                it.copy(
                    searchError = UiText.Resource(Res.string.error_participant_already_in_chat)
                )
            }
            return
        }

        _state.update {
            it.copy(
                selectedChatParticipants = it.selectedChatParticipants + searchResult,
                currentSearchResult = null
            )
        }
        _state.value.queryTextFieldState.clearText()
    }

    private fun addParticipantsToChat() {
        if (state.value.selectedChatParticipants.isEmpty()) {
            return
        }

        val chatId = _chatId.value ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }

            val participantIds = state
                .value
                .selectedChatParticipants
                .map { it.id }
            when (val result = chatRepository.addParticipants(chatId, participantIds)) {
                is Result.Failure -> {
                    _state.update {
                        it.copy(submitError = result.toUiText())
                    }
                }
                is Result.Success -> {
                    eventChannel.send(ManageChatEvent.OnChatMembersModified)
                }
            }

            _state.update { it.copy(isSubmitting = false) }
        }
    }
}