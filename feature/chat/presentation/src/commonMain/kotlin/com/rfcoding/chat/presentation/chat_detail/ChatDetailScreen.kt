package com.rfcoding.chat.presentation.chat_detail

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.domain.models.ConnectionState
import com.rfcoding.chat.presentation.chat_detail.components.ChatDetailHeader
import com.rfcoding.chat.presentation.chat_detail.components.MessageBox
import com.rfcoding.chat.presentation.chat_detail.components.MessageList
import com.rfcoding.chat.presentation.components.ChatHeader
import com.rfcoding.chat.presentation.model.ChatUi
import com.rfcoding.chat.presentation.model.MessageUi
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.presentation.util.UiText
import com.rfcoding.core.presentation.util.clearFocusOnTap
import com.rfcoding.core.presentation.util.currentDeviceConfiguration
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
    viewModel: ChatDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(chatId) {
        viewModel.onAction(ChatDetailAction.OnSelectChat(chatId))
    }

    BackHandler(
        enabled = !isDetailPresent
    ) {
        viewModel.onAction(ChatDetailAction.OnSelectChat(null))
        onBack()
    }

    ChatDetailScreen(
        state = state,
        isDetailPresent = isDetailPresent,
        onAction = { action ->
            when (action) {
                ChatDetailAction.OnBackClick -> onBack()
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
    onAction: (ChatDetailAction) -> Unit,
) {
    val configuration = currentDeviceConfiguration()
    val messageListState = rememberLazyListState()

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
                    ChatHeader {
                        ChatDetailHeader(
                            chatUi = state.chatUi,
                            isDetailPresent = isDetailPresent,
                            isChatOptionsDropDownOpen = state.isChatOptionsOpen,
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

                    MessageList(
                        messages = state.messages,
                        listState = messageListState,
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

                    AnimatedVisibility(
                        visible = !configuration.isWideScreen && state.chatUi != null
                    ) {
                        MessageBox(
                            messageTextFieldState = state.messageTextFieldState,
                            isTextInputEnabled = state.canSendMessage,
                            connectionState = state.connectionState,
                            onSendClick = {
                                onAction(ChatDetailAction.OnSendMessageClick)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }

                if (configuration.isWideScreen) {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                AnimatedVisibility(
                    visible = configuration.isWideScreen && state.chatUi != null
                ) {
                    MessageBox(
                        messageTextFieldState = state.messageTextFieldState,
                        isTextInputEnabled = state.canSendMessage,
                        connectionState = state.connectionState,
                        onSendClick = {
                            onAction(ChatDetailAction.OnSendMessageClick)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
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
                elevation = if (isCornersRounded) 4.dp else 0.dp,
                shape = if (isCornersRounded) {
                    RoundedCornerShape(16.dp)
                } else RectangleShape
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = if (isCornersRounded) {
                    RoundedCornerShape(16.dp)
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
                            isMenuOpen = false,
                            formattedSentTime = UiText.DynamicText("Friday 6:45 PM")
                        )
                    } else {
                        MessageUi.OtherUserMessage(
                            id = Uuid.random().toString(),
                            content = "Hello world other $it",
                            sender = otherParticipants[0],
                            formattedSentTime = UiText.DynamicText("Friday 6:45 PM")
                        )
                    }
                },
                messageTextFieldState = rememberTextFieldState(
                    initialText = "This is a new message about to be sent! Hoping that it would span to the next line, yey!"
                ),
                canSendMessage = true,
                connectionState = ConnectionState.CONNECTED
            ),
            isDetailPresent = false,
            onAction = {}
        )
    }
}