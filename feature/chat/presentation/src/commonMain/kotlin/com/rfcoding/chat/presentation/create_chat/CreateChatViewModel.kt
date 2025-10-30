package com.rfcoding.chat.presentation.create_chat

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.chat.domain.chat.ChatService
import com.rfcoding.chat.presentation.mappers.toUi
import com.rfcoding.core.domain.auth.AuthConstants
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.domain.auth.User
import com.rfcoding.core.domain.util.Result
import com.rfcoding.core.presentation.util.toUiText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class CreateChatViewModel(
    private val chatService: ChatService,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private var hasLoadedInitialData = false
    private lateinit var currentUser: User

    private val _state = MutableStateFlow(CreateChatState())
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
            initialValue = CreateChatState()
        )

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
        if (query.isBlank()
            || query.length !in AuthConstants.VALID_USERNAME_LENGTH_RANGE
            || query == currentUser.username
            || query == currentUser.email
        ) {
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
                is Result.Failure -> { _state.update { it.copy(searchError = result.toUiText()) }
                }
                is Result.Success -> {
                    _state.update { it.copy(currentSearchResult = result.data.toUi()) }
                }
            }

            _state.update { it.copy(isSearching = false) }
        }
    }

    fun onAction(action: CreateChatAction) {
        when (action) {
            CreateChatAction.OnAddClick -> addParticipant()
            CreateChatAction.OnCreateChatClick -> {}
            CreateChatAction.OnDismissDialog -> Unit
        }
    }

    private fun addParticipant() {
        state.value.currentSearchResult?.let { participant ->
            val updatedSelectedParticipants = state.value.selectedChatParticipants +
                    participant
            _state.update { it.copy(selectedChatParticipants = updatedSelectedParticipants) }
        }
    }
}