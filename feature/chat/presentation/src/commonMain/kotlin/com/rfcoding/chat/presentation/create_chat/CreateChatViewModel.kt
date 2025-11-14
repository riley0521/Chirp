package com.rfcoding.chat.presentation.create_chat

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.error_participant_already_in_chat
import chirp.feature.chat.presentation.generated.resources.error_you_are_already_in_chat
import com.rfcoding.chat.domain.chat.ChatRepository
import com.rfcoding.chat.domain.chat.ChatService
import com.rfcoding.chat.presentation.components.manage_chat.ManageChatAction
import com.rfcoding.chat.presentation.components.manage_chat.ManageChatState
import com.rfcoding.chat.presentation.mappers.toUi
import com.rfcoding.core.domain.auth.AuthConstants
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.domain.auth.User
import com.rfcoding.core.domain.util.Result
import com.rfcoding.core.presentation.util.UiText
import com.rfcoding.core.presentation.util.toUiText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class CreateChatViewModel(
    private val chatRepository: ChatRepository,
    private val chatService: ChatService,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private var hasLoadedInitialData = false
    private lateinit var currentUser: User

    private val _state = MutableStateFlow(ManageChatState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                initCurrentUser()
                observeQuery()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ManageChatState()
        )

    private val eventChannel = Channel<CreateChatEvent>()
    val events = eventChannel.receiveAsFlow()

    private val queryFlow = snapshotFlow { state.value.queryTextFieldState.text.toString() }

    private fun initCurrentUser() {
        viewModelScope.launch {
            currentUser = sessionStorage
                .observeAuthenticatedUser()
                .firstOrNull()
                ?.user ?: throw IllegalStateException("User is not logged in.")
        }
    }

    private fun observeQuery() {
        queryFlow
            .debounce(500L)
            .onEach { query ->
                findByEmailOrUsername(query)
            }.launchIn(viewModelScope)
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

        if (query == currentUser.email || query == currentUser.username) {
            _state.update {
                it.copy(
                    currentSearchResult = null,
                    searchError = UiText.Resource(Res.string.error_you_are_already_in_chat)
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
            ManageChatAction.OnAddClick -> addParticipant()
            ManageChatAction.OnPrimaryButtonClick -> createChat()
            ManageChatAction.OnDismissDialog -> Unit
            is ManageChatAction.OnChatSelect -> Unit
        }
    }

    private fun addParticipant() {
        state.value.currentSearchResult?.let { participant ->
            val isParticipantAlreadySelected = state
                .value
                .selectedChatParticipants
                .any { it.id == participant.id }
            if (isParticipantAlreadySelected) {
                _state.update {
                    it.copy(searchError = UiText.Resource(Res.string.error_participant_already_in_chat))
                }
                return@let
            }

            _state.update {
                it.copy(
                    selectedChatParticipants = it.selectedChatParticipants + participant,
                    currentSearchResult = null
                )
            }
            _state.value.queryTextFieldState.clearText()
        }
    }

    private fun createChat() {
        if (state.value.selectedChatParticipants.isEmpty()) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }

            val participantIds = state.value.selectedChatParticipants.map { it.id }
            when (val result = chatRepository.createChat(participantIds)) {
                is Result.Failure -> {
                    _state.update { it.copy(submitError = result.toUiText()) }
                }
                is Result.Success -> {
                    eventChannel.send(CreateChatEvent.OnChatCreated(result.data))
                }
            }

            _state.update { it.copy(isSubmitting = false) }
        }
    }
}