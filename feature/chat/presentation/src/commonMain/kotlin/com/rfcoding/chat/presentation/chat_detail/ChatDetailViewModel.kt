package com.rfcoding.chat.presentation.chat_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.chat.domain.chat.ChatRepository
import com.rfcoding.chat.domain.models.ChatInfo
import com.rfcoding.chat.presentation.mappers.toUi
import com.rfcoding.chat.presentation.model.ChatUi
import com.rfcoding.chat.presentation.model.MessageUi
import com.rfcoding.chat.presentation.util.getLocalDateFromInstant
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.presentation.util.UiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
class ChatDetailViewModel(
    private val chatRepository: ChatRepository,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private val _chatId = MutableStateFlow<String?>(null)
    private val chatInfoFlow = _chatId
        .flatMapLatest { chatId ->
            if (chatId != null) {
                chatRepository.getChatInfoById(chatId)
            } else emptyFlow()
        }

    private var hasLoadedInitialData = false
    private val _state = MutableStateFlow(ChatDetailState())
    private val stateWithMessages = combine(
        _state,
        chatInfoFlow,
        sessionStorage.observeAuthenticatedUser()
    ) { curState, chatInfo, authInfo ->
        val (chatUi, messageUiList) = getChatUiAndMessages(
            chatInfo = chatInfo,
            localUserId = authInfo?.user?.id ?: return@combine ChatDetailState(),
            localMessages = curState
                .messages
                .filterIsInstance<MessageUi.LocalUserMessage>()
        )

        curState.copy(
            chatUi = chatUi,
            messages = messageUiList
        )
    }

    val state = stateWithMessages
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

    private fun getChatUiAndMessages(
        chatInfo: ChatInfo,
        localUserId: String,
        localMessages: List<MessageUi.LocalUserMessage>
    ): Pair<ChatUi, List<MessageUi>> {
        val chatUi = chatInfo.chat.toUi(
            localUserId = localUserId,
            lastMessageUsername = null,
            affectedUsernamesForEvent = emptyList()
        )

        val messageUiList = mutableListOf<MessageUi>()
        val messagesGroupedByDate = chatInfo.messages.associateBy {
            getLocalDateFromInstant(it.message.createdAt)
        }
        val participantUiList = chatInfo.chat.participants.map { it?.toUi() }

        messagesGroupedByDate.forEach { (date, messageWithSender) ->
            messageUiList.add(
                MessageUi.DateSeparator(
                    id = Uuid.random().toString(),
                    date = UiText.DynamicText("TODO!")
                )
            )

            val localMessage = localMessages.firstOrNull { it.id == messageWithSender.message.id }
            messageUiList.add(
                messageWithSender.toUi(
                    localUserId = localUserId,
                    isMenuOpen = localMessage?.isMenuOpen == true,
                    getParticipantById = { participantId ->
                        participantUiList.find { it?.id == participantId }
                    }
                )
            )
        }

        return chatUi to messageUiList
    }

    fun onAction(action: ChatDetailAction) {
        when (action) {
            ChatDetailAction.OnChatMembersClick -> {}
            ChatDetailAction.OnChatOptionsClick -> {}
            is ChatDetailAction.OnDeleteMessageClick -> {}
            ChatDetailAction.OnDismissChatOptions -> {}
            ChatDetailAction.OnDismissMessageMenu -> {}
            ChatDetailAction.OnLeaveChatClick -> {}
            is ChatDetailAction.OnMessageLongClick -> {}
            is ChatDetailAction.OnRetryClick -> {}
            ChatDetailAction.OnScrollToTop -> {}
            is ChatDetailAction.OnSelectChat -> switchChat(action.chatId)
            ChatDetailAction.OnSendMessageClick -> {}
            is ChatDetailAction.OnImageClick -> Unit // TODO
            ChatDetailAction.OnBackClick -> Unit
        }
    }

    private fun switchChat(chatId: String?) {
        _chatId.update { chatId }
        if (chatId == null) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            chatRepository.fetchChatById(chatId)
            _state.update { it.copy(isLoading = false) }
        }
    }
}