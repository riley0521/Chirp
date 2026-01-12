package com.rfcoding.chat.presentation.chat_detail

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.today
import com.rfcoding.chat.domain.chat.ChatConnectionClient
import com.rfcoding.chat.domain.chat.ChatRepository
import com.rfcoding.chat.domain.message.ChatMessageService
import com.rfcoding.chat.domain.message.MessageRepository
import com.rfcoding.chat.domain.models.ChatMessage
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.domain.models.ConnectionState
import com.rfcoding.chat.domain.models.Media
import com.rfcoding.chat.domain.models.MediaProgress
import com.rfcoding.chat.domain.models.OutgoingNewMessage
import com.rfcoding.chat.presentation.mappers.toUi
import com.rfcoding.chat.presentation.mappers.toUiList
import com.rfcoding.chat.presentation.model.MessageUi
import com.rfcoding.core.designsystem.components.textfields.ImageData
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.domain.logging.ChirpLogger
import com.rfcoding.core.domain.util.Paginator
import com.rfcoding.core.domain.util.Result
import com.rfcoding.core.presentation.util.UiText
import com.rfcoding.core.presentation.util.toUiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
class ChatDetailViewModel(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val client: ChatConnectionClient,
    private val chatMessageService: ChatMessageService,
    private val sessionStorage: SessionStorage,
    private val logger: ChirpLogger
) : ViewModel() {

    private val _chatId = MutableStateFlow<String?>(null)
    private val chatInfoFlow = _chatId
        .onEach { chatId ->
            if (chatId != null) {
                setupPaginatorForChat(chatId)
                paginateItems()
            } else {
                currentPaginator = null
            }
        }
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
        val localUserId = authInfo?.user?.id ?: return@combine ChatDetailState()
        val chatUi = chatInfo.chat.toUi(
            localUserId = localUserId,
            lastMessageUsername = null,
            affectedUsernamesForEvent = emptyList()
        )

        curState.copy(
            chatUi = chatUi,
            messages = chatInfo.messages.toUiList(localUserId)
        )
    }

    val state = _chatId
        .flatMapLatest { chatId ->
            if (chatId != null) {
                stateWithMessages
            } else {
                _state
            }
        }
        .onStart {
            if (!hasLoadedInitialData) {
                observeConnectionState()
                observeChatMessages()
                userTypingFlow.launchIn(viewModelScope)
                otherUsersTypingFlow.launchIn(viewModelScope)
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ChatDetailState()
        )

    private var currentPaginator: Paginator<String?, ChatMessage>? = null

    private val eventChannel = Channel<ChatDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private val userTypingFlow = snapshotFlow { _state.value.messageTextFieldState.text.toString() }
        .combine(_chatId) { messageContent, chatId ->
            if (chatId == null || messageContent.isBlank()) {
                return@combine
            }

            client.sendTypingIndicator(chatId)
        }

    private val otherUsersTypingFlow = client
        .usersTypingState
        .combine(stateWithMessages) { users, curState ->
            val userIds = users.map { it.userId }
            val otherUsersTyping = curState.chatUi?.participants?.filterNotNull()?.filter {
                it.id in userIds
            } ?: return@combine

            logger.debug("Users typing: ${otherUsersTyping.joinToString(", ") { it.username }}")

            _state.update {
                it.copy(
                    otherUsersTyping = otherUsersTyping
                )
            }
        }

    private fun observeConnectionState() {
        client
            .connectionState
            .onEach { connectionState ->
                if (connectionState == ConnectionState.CONNECTED) {
                    currentPaginator?.loadNextItems()
                }

                _state.update { it.copy(connectionState = connectionState) }
            }.launchIn(viewModelScope)
    }

    private fun observeChatMessages() {
        val currentMessages = state
            .map { it.messages }
            .distinctUntilChanged()

        val newMessages = _chatId.flatMapLatest { chatId ->
            if (chatId != null) {
                messageRepository.getMessagesForChat(chatId)
            } else emptyFlow()
        }

        val isNearBottom = state.map { it.isNearBottom }.distinctUntilChanged()

        combine(
            currentMessages,
            newMessages,
            isNearBottom
        ) { currentMessages, newMessages, isNearBottom ->
            val lastNewId = newMessages.firstOrNull()?.message?.id
            val lastCurrentId = currentMessages.firstOrNull()?.id

            if (lastNewId != lastCurrentId && isNearBottom) {
                eventChannel.send(ChatDetailEvent.OnNewMessage)
            }
        }.launchIn(viewModelScope)
    }

    private fun sendMessage() {
        val currentChatId = _chatId.value ?: return
        val content = state.value.messageTextFieldState.text.toString().trim()
        if (content.isBlank()) {
            return
        }

        viewModelScope.launch {
            val messageId = Uuid.random().toString()
            val imagesBytes = state.value.images.map { it.bytes }

            val result = messageRepository.sendLocalMessage(
                message = OutgoingNewMessage(
                    messageId = messageId,
                    chatId = currentChatId,
                    content = content
                ),
                imagesToUpload = imagesBytes,
                audioBytes = null, // TODO
                audioDurationInSeconds = 0
            )

            when (result) {
                is Result.Failure -> Unit
                is Result.Success -> {
                    state.value.messageTextFieldState.clearText()
                    val mediasToUpload = result.data
                    when {
                        imagesBytes.isNotEmpty() -> {
                            val allSuccessful = uploadMedias(currentChatId, messageId, mediasToUpload)
                            if (!allSuccessful) {
                                messageRepository.changeDeliveryStatusOfLocalMessage(
                                    messageId = messageId,
                                    status = ChatMessageDeliveryStatus.FAILED
                                )
                                return@launch
                            }

                            messageRepository.sendMessage(messageId)
                        }
                        else -> {
                            messageRepository.sendMessage(messageId)
                        }
                    }
                }
            }
        }
    }

    private suspend fun uploadMedias(
        chatId: String,
        messageId: String,
        medias: List<Media>
    ): Boolean {
        var allSuccessful = true
        medias.forEach { media ->
            val bytes = (media.progress as? MediaProgress.Sending)?.bytes ?: return@forEach

            when (val result = chatMessageService.uploadFile(chatId, bytes)) {
                is Result.Failure -> {
                    messageRepository.updateMediaProgress(
                        messageId = messageId,
                        name = media.name,
                        progress = MediaProgress.Failed
                    )
                    allSuccessful = false
                }
                is Result.Success -> {
                    messageRepository.updateMediaProgress(
                        messageId = messageId,
                        name = media.name,
                        progress = MediaProgress.Sent(publicUrl = result.data)
                    )
                }
            }
        }

        return allSuccessful
    }

    private fun retryMessage(message: MessageUi.LocalUserMessage) {
        viewModelScope.launch {
            val currentChatId = _chatId.value ?: return@launch

            // Change the status from failed to sending again...
            messageRepository.changeDeliveryStatusOfLocalMessage(
                messageId = message.id,
                status = ChatMessageDeliveryStatus.SENDING
            )

            // Get and upload the remaining failed medias.
            val pendingMedias = messageRepository.getPendingMedias(message.id)
            val allSuccessful = uploadMedias(currentChatId, message.id, pendingMedias)
            if (!allSuccessful) {
                messageRepository.changeDeliveryStatusOfLocalMessage(
                    messageId = message.id,
                    status = ChatMessageDeliveryStatus.FAILED
                )
                return@launch
            }

            // Then we can finally send the message to remote server.
            messageRepository.sendMessage(message.id)
        }
    }

    private fun setupPaginatorForChat(chatId: String) {
        currentPaginator = Paginator(
            initialKey = null,
            onLoadUpdated = { isLoading ->
                _state.update { it.copy(isPaginationLoading = isLoading) }
            },
            onRequest = { nextKey ->
                messageRepository.fetchMessages(
                    chatId = chatId,
                    before = nextKey
                )
            },
            getNextKey = { items ->
                items.minOfOrNull { it.createdAt }?.toString()
            },
            onError = { error ->
                error?.let {
                    _state.update {
                        it.copy(paginationError = Result.Failure(error).toUiText())
                    }
                }
            },
            onSuccess = { items, _ ->
                _state.update {
                    it.copy(
                        endReached = items.isEmpty(),
                        paginationError = null
                    )
                }
            }
        )

        _state.update {
            it.copy(
                endReached = false,
                isPaginationLoading = false,
                paginationError = null
            )
        }
    }

    fun onAction(action: ChatDetailAction) {
        when (action) {
            ChatDetailAction.OnChatOptionsClick -> {
                _state.update { it.copy(isChatOptionsOpen = true) }
            }
            is ChatDetailAction.OnDeleteMessageClick -> deleteMessage(action.message)
            ChatDetailAction.OnDismissChatOptions -> {
                _state.update { it.copy(isChatOptionsOpen = false) }
            }
            ChatDetailAction.OnDismissMessageMenu -> dismissMessageMenu()
            ChatDetailAction.OnLeaveChatClick -> leaveChat()
            is ChatDetailAction.OnMessageLongClick -> openMessageMenu(action.message)
            is ChatDetailAction.OnRetryClick -> retryMessage(action.message)
            ChatDetailAction.OnScrollToTop -> paginateItems()
            is ChatDetailAction.OnFirstVisibleIndexChanged -> updateNearBottom(action.index)
            is ChatDetailAction.OnTopVisibleIndexChanged -> updateBanner(action.topVisibleIndex)
            ChatDetailAction.OnHideBanner -> {
                _state.update {
                    it.copy(
                        bannerState = BannerState(
                            formattedDate = null,
                            isVisible = false
                        )
                    )
                }
            }
            is ChatDetailAction.OnSelectChat -> switchChat(action.chatId)
            ChatDetailAction.OnSendMessageClick -> sendMessage()
            is ChatDetailAction.OnImagesSelected -> addSelectedImages(action.values)
            is ChatDetailAction.OnRemoveImage -> removeImage(action.id)
            is ChatDetailAction.OnImageClick -> {
                action.value
            }
            ChatDetailAction.OnBackClick -> Unit
            ChatDetailAction.OnChatMembersClick -> Unit
            ChatDetailAction.OnAttachImageClick -> Unit
        }
    }

    private fun removeImage(id: String) {
        _state.update {
            it.copy(
                images = it.images.filterNot { data -> data.id == id }
            )
        }
    }

    private fun addSelectedImages(values: List<ByteArray>) {
        val currentImages = state.value.images

        val imagesToAdd = values.mapNotNull { bytes ->
            if (currentImages.any { it.bytes.contentEquals(bytes) }) {
                return@mapNotNull null
            }

            ImageData(
                id = Uuid.random().toString(),
                bytes = bytes
            )
        }

        _state.update {
            it.copy(
                // Only upload 10 images at once.
                images = (it.images + imagesToAdd).take(10)
            )
        }
    }

    private fun updateNearBottom(firstVisibleIndex: Int) {
        _state.update {
            it.copy(
                isNearBottom = firstVisibleIndex <= 4 // Less than 5 items, unfortunately it includes date separator
            )
        }
    }

    private fun updateBanner(topVisibleIndex: Int) {
        val messages = state.value.messages
        val banner = calculateBannerDateFromIndex(
            messages = messages,
            index = topVisibleIndex
        )


        _state.update {
            it.copy(
                bannerState = BannerState(
                    formattedDate = banner,
                    isVisible = banner != null
                )
            )
        }
    }

    private fun calculateBannerDateFromIndex(
        messages: List<MessageUi>,
        index: Int
    ): UiText? {
        if (messages.isEmpty() || index < 0 || index >= messages.size) {
            return null
        }

        val nearestDateSeparator = (index until messages.size)
            .asSequence()
            .mapNotNull {
                val item = messages.getOrNull(it)
                if (item is MessageUi.DateSeparator) item.date else null
            }
            .firstOrNull()

        return when (nearestDateSeparator) {
            is UiText.Resource -> {
                if (nearestDateSeparator.id == Res.string.today) null else nearestDateSeparator
            }
            else -> nearestDateSeparator
        }
    }

    private fun paginateItems() {
        viewModelScope.launch {
            currentPaginator?.loadNextItems()
        }
    }

    private fun openMessageMenu(message: MessageUi.LocalUserMessage) {
        _state.update { it.copy(messageWithOpenMenu = message) }
    }

    private fun dismissMessageMenu() {
        _state.update { it.copy(messageWithOpenMenu = null) }
    }

    private fun deleteMessage(message: MessageUi.LocalUserMessage) {
        viewModelScope.launch {
            dismissMessageMenu()
            messageRepository.deleteMessage(message.id)
        }
    }

    private fun switchChat(chatId: String?) {
        _chatId.update { chatId }
        if (chatId == null) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(100L)

            chatRepository.fetchChatById(chatId)
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun resetState() {
        _state.value.messageTextFieldState.clearText()
        _state.update {
            it.copy(
                chatUi = null,
                isLoading = false,
                messages = emptyList(),
                error = null,
                isPaginationLoading = false,
                paginationError = null,
                endReached = false,
                messageWithOpenMenu = null,
                bannerState = BannerState(),
                isChatOptionsOpen = false,
                isNearBottom = false
            )
        }
    }

    private fun leaveChat() {
        val chatId = _chatId.value ?: return
        if (!state.value.chatUi!!.isGroupChat) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isChatOptionsOpen = false, isLoading = true) }
            delay(100L)

            when (val result = chatRepository.leaveChat(chatId)) {
                is Result.Failure -> {
                    _state.update { it.copy(error = result.toUiText()) }
                }
                is Result.Success -> {
                    switchChat(null)
                    resetState()
                    eventChannel.send(ChatDetailEvent.LeaveChatSuccessful)
                }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }
}