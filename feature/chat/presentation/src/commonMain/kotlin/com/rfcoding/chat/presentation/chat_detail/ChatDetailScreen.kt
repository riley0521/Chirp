package com.rfcoding.chat.presentation.chat_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.no_chat_selected
import chirp.feature.chat.presentation.generated.resources.select_a_chat
import chirp.feature.chat.presentation.generated.resources.x_typing
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.domain.models.ConnectionState
import com.rfcoding.chat.presentation.chat_detail.components.ChatDetailHeader
import com.rfcoding.chat.presentation.chat_detail.components.DateChip
import com.rfcoding.chat.presentation.chat_detail.components.MessageBannerListener
import com.rfcoding.chat.presentation.chat_detail.components.MessageBox
import com.rfcoding.chat.presentation.chat_detail.components.MessageList
import com.rfcoding.chat.presentation.chat_detail.components.PaginationScrollListener
import com.rfcoding.chat.presentation.components.ChatHeader
import com.rfcoding.chat.presentation.components.EmptyListSection
import com.rfcoding.chat.presentation.model.ChatUi
import com.rfcoding.chat.presentation.model.MediaUi
import com.rfcoding.chat.presentation.model.MessageUi
import com.rfcoding.chat.presentation.profile.mediapicker.rememberMultipleImagePickerLauncher
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.presentation.permissions.Permission
import com.rfcoding.core.presentation.permissions.PermissionState
import com.rfcoding.core.presentation.permissions.rememberPermissionController
import com.rfcoding.core.presentation.util.ObserveAsEvents
import com.rfcoding.core.presentation.util.UiText
import com.rfcoding.core.presentation.util.clearFocusOnTap
import com.rfcoding.core.presentation.util.currentDeviceConfiguration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatDetailRoot(
    chatId: String?,
    isDetailPresent: Boolean,
    onBack: () -> Unit,
    onViewChatMembersOrManageChatClick: () -> Unit,
    viewModel: ChatDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val messageListState = rememberLazyListState()
    val permissionController = rememberPermissionController()

    val multipleImageLauncher = rememberMultipleImagePickerLauncher(
        onResult = { data ->
            viewModel.onAction(ChatDetailAction.OnImagesSelected(data.map { it.bytes }))
        }
    )

    LaunchedEffect(chatId) {
        viewModel.onAction(ChatDetailAction.OnSelectChat(chatId))
    }

    LaunchedEffect(chatId, state.messages) {
        if (state.messages.isNotEmpty()) {
            messageListState.scrollToItem(0)
        }
    }

    BackHandler(
        enabled = !isDetailPresent
    ) {
        scope.launch {
            // Add artificial delay to wait for the back animation to finish before unselecting chat.
            delay(300L)
            viewModel.onAction(ChatDetailAction.OnSelectChat(null))
        }
        onBack()
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ChatDetailEvent.LeaveChatSuccessful -> onBack()
            ChatDetailEvent.OnNewMessage -> {
                scope.launch {
                    // Add artificial delay to wait for the current messages to match the new messages.
                    delay(50L)
                    messageListState.animateScrollToItem(0)
                }
            }
            ChatDetailEvent.RequestAudioPermission -> {
                when (permissionController.requestPermission(Permission.RECORD_AUDIO)) {
                    PermissionState.GRANTED -> {
                        viewModel.onAction(ChatDetailAction.OnAudioPermissionGranted)
                    }
                    else -> Unit
                }
            }
        }
    }

    ChatDetailScreen(
        state = state,
        isDetailPresent = isDetailPresent,
        messageListState = messageListState,
        onAction = { action ->
            when (action) {
                ChatDetailAction.OnBackClick -> {
                    viewModel.onAction(ChatDetailAction.OnSelectChat(null))
                    onBack()
                }
                ChatDetailAction.OnChatMembersClick -> onViewChatMembersOrManageChatClick()
                ChatDetailAction.OnAttachImageClick -> {
                    multipleImageLauncher.launch()
                }
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
fun ChatDetailScreen(
    state: ChatDetailState,
    isDetailPresent: Boolean,
    messageListState: LazyListState,
    onAction: (ChatDetailAction) -> Unit,
) {
    val configuration = currentDeviceConfiguration()

    val realMessageItemCount = remember(state.messages) {
        state.messages.filterNot { it is MessageUi.DateSeparator }.size
    }

    LaunchedEffect(messageListState) {
        snapshotFlow {
            messageListState.firstVisibleItemIndex to messageListState.layoutInfo.totalItemsCount
        }.filter { (index, total) ->
            index >= 0 && total > 0
        }.collect { (index, _) ->
            onAction(ChatDetailAction.OnFirstVisibleIndexChanged(index))
        }
    }

    MessageBannerListener(
        lazyListState = messageListState,
        messages = state.messages,
        isBannerVisible = state.bannerState.isVisible,
        onShowBanner = { index ->
            onAction(ChatDetailAction.OnTopVisibleIndexChanged(index))
        },
        onHide = {
            onAction(ChatDetailAction.OnHideBanner)
        }
    )

    PaginationScrollListener(
        lazyListState = messageListState,
        itemCount = realMessageItemCount,
        isPaginationLoading = state.isPaginationLoading,
        isEndReached = state.endReached,
        onNearTop = {
            onAction(ChatDetailAction.OnScrollToTop)
        }
    )

    var headerHeight by remember {
        mutableStateOf(0.dp)
    }
    val density = LocalDensity.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = if (!configuration.isWideScreen) {
            MaterialTheme.colorScheme.surface
        } else MaterialTheme.colorScheme.extended.surfaceLower
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .clearFocusOnTap()
                .padding(innerPadding)
                .then(
                    if (configuration.isWideScreen) {
                        Modifier.padding(horizontal = 8.dp)
                    } else Modifier
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DynamicRoundedCornerColumn(
                    isCornersRounded = configuration.isWideScreen,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (state.chatUi == null) {
                        EmptyListSection(
                            title = stringResource(Res.string.no_chat_selected),
                            description = stringResource(Res.string.select_a_chat),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        ChatHeader(
                            modifier = Modifier
                                .onSizeChanged {
                                    headerHeight = with(density) { it.height.toDp() }
                                }
                        ) {
                            ChatDetailHeader(
                                chatUi = state.chatUi,
                                isDetailPresent = isDetailPresent,
                                isChatOptionsDropDownOpen = state.isChatOptionsOpen,
                                isLoading = state.isLoading,
                                onBackClick = {
                                    onAction(ChatDetailAction.OnBackClick)
                                },
                                onChatOptionsClick = {
                                    onAction(ChatDetailAction.OnChatOptionsClick)
                                },
                                onDismissChatOptions = {
                                    onAction(ChatDetailAction.OnDismissChatOptions)
                                },
                                onManageChatClick = {
                                    onAction(ChatDetailAction.OnChatMembersClick)
                                },
                                onLeaveChatClick = {
                                    onAction(ChatDetailAction.OnLeaveChatClick)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }

                        if (state.isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            MessageList(
                                messages = state.messages,
                                listState = messageListState,
                                messageWithOpenMenu = state.messageWithOpenMenu,
                                paginationError = state.paginationError,
                                isPaginationLoading = state.isPaginationLoading,
                                onRetryPaginationClick = {
                                    onAction(ChatDetailAction.OnScrollToTop)
                                },
                                onMessageLongClick = {
                                    onAction(ChatDetailAction.OnMessageLongClick(it))
                                },
                                onMessageRetryClick = {
                                    onAction(ChatDetailAction.OnRetryClick(it))
                                },
                                onDeleteMessageClick = {
                                    onAction(ChatDetailAction.OnDeleteMessageClick(it))
                                },
                                onImageClick = {
                                    onAction(ChatDetailAction.OnImageClick(it))
                                },
                                onDismissMessageMenu = {
                                    onAction(ChatDetailAction.OnDismissMessageMenu)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }

                        if (state.otherUsersTyping.isNotEmpty()) {
                            Text(
                                text = pluralStringResource(
                                    Res.plurals.x_typing,
                                    state.otherUsersTyping.size,
                                    state.typingUsers
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.extended.textPlaceholder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 16.dp,
                                        top = 8.dp,
                                        end = 16.dp,
                                        bottom = 24.dp
                                    )
                            )
                        }

                        AnimatedVisibility(
                            visible = !configuration.isWideScreen
                        ) {
                            MessageBox(
                                messageTextFieldState = state.messageTextFieldState,
                                isTextInputEnabled = state.canSendMessage,
                                connectionState = state.connectionState,
                                images = state.images,
                                onSendClick = {
                                    onAction(ChatDetailAction.OnSendMessageClick)
                                },
                                onAttachImageClick = {
                                    onAction(ChatDetailAction.OnAttachImageClick)
                                },
                                onRemoveImage = {
                                    onAction(ChatDetailAction.OnRemoveImage(it))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        vertical = 8.dp,
                                        horizontal = 16.dp
                                    )
                            )
                        }
                    }
                }

                if (configuration.isWideScreen) {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                AnimatedVisibility(
                    visible = configuration.isWideScreen && state.chatUi != null
                ) {
                    DynamicRoundedCornerColumn(
                        isCornersRounded = true
                    ) {
                        MessageBox(
                            messageTextFieldState = state.messageTextFieldState,
                            isTextInputEnabled = state.canSendMessage,
                            connectionState = state.connectionState,
                            images = state.images,
                            onSendClick = {
                                onAction(ChatDetailAction.OnSendMessageClick)
                            },
                            onAttachImageClick = {
                                onAction(ChatDetailAction.OnAttachImageClick)
                            },
                            onRemoveImage = {
                                onAction(ChatDetailAction.OnRemoveImage(it))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = state.bannerState.isVisible,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = headerHeight + 16.dp),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                state.bannerState.formattedDate?.let { bannerText ->
                    DateChip(date = bannerText.asString())
                }
            }
        }
    }
}

@Composable
private fun DynamicRoundedCornerColumn(
    isCornersRounded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = if (isCornersRounded) 8.dp else 0.dp,
                shape = if (isCornersRounded) {
                    RoundedCornerShape(24.dp)
                } else RectangleShape,
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = if (isCornersRounded) {
                    RoundedCornerShape(24.dp)
                } else RectangleShape
            )
    ) {
        content()
    }
}

@Preview
@Composable
private fun ChatDetailWithEmptyMessagesPreview() {
    ChirpTheme {
        val localParticipant = ChatParticipantUi(
            id = "1",
            username = "matthew",
            initial = "MA"
        )
        val otherParticipants = listOf(
            ChatParticipantUi(
                id = "2",
                username = "luke",
                initial = "LU"
            ),
            ChatParticipantUi(
                id = "3",
                username = "john",
                initial = "JO"
            )
        )

        ChatDetailScreen(
            state = ChatDetailState(
                chatUi = ChatUi(
                    id = "1",
                    localParticipant = localParticipant,
                    participants = otherParticipants,
                    lastMessage = null,
                    lastMessageUsername = null,
                    affectedUsernamesForEvent = emptyList(),
                    isGroupChat = true,
                    creatorId = "1",
                    name = null
                )
            ),
            isDetailPresent = false,
            messageListState = rememberLazyListState(),
            onAction = {}
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
@Preview
@Composable
private fun ChatDetailWithMessagesPreview() {
    ChirpTheme {
        val localParticipant = ChatParticipantUi(
            id = "1",
            username = "matthew",
            initial = "MA"
        )
        val otherParticipants = listOf(
            ChatParticipantUi(
                id = "2",
                username = "luke",
                initial = "LU"
            ),
            ChatParticipantUi(
                id = "3",
                username = "john",
                initial = "JO"
            ),
            ChatParticipantUi(
                id = "4",
                username = "mark",
                initial = "MA"
            ),
            ChatParticipantUi(
                id = "5",
                username = "Paul",
                initial = "PA"
            )
        )

        ChatDetailScreen(
            state = ChatDetailState(
                chatUi = ChatUi(
                    id = "1",
                    localParticipant = localParticipant,
                    participants = otherParticipants,
                    lastMessage = null,
                    lastMessageUsername = null,
                    affectedUsernamesForEvent = emptyList(),
                    isGroupChat = true,
                    creatorId = "1",
                    name = null
                ),
                messages = (1..20).map {
                    val showLocalMessage = Random.nextBoolean()
                    if (showLocalMessage) {
                        MessageUi.LocalUserMessage(
                            id = Uuid.random().toString(),
                            content = "Hello world local $it",
                            deliveryStatus = ChatMessageDeliveryStatus.SENT,
                            formattedSentTime = UiText.DynamicText("Friday 6:45 PM"),
                            media = MediaUi.NoMedia
                        )
                    } else {
                        MessageUi.OtherUserMessage(
                            id = Uuid.random().toString(),
                            content = "Hello world other $it",
                            sender = otherParticipants[0],
                            formattedSentTime = UiText.DynamicText("Friday 6:45 PM"),
                            media = MediaUi.NoMedia
                        )
                    }
                },
                messageTextFieldState = rememberTextFieldState(
                    initialText = "This is a new message about to be sent! Hoping that it would span to the next line, yey!"
                ),
                //canSendMessage = true,
                connectionState = ConnectionState.CONNECTED,
                otherUsersTyping = otherParticipants
            ),
            isDetailPresent = false,
            messageListState = rememberLazyListState(),
            onAction = {}
        )
    }
}