package com.rfcoding.chat.presentation.chat_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.cancel
import chirp.feature.chat.presentation.generated.resources.create_chat
import chirp.feature.chat.presentation.generated.resources.dialog_logout_description
import chirp.feature.chat.presentation.generated.resources.dialog_logout_title
import chirp.feature.chat.presentation.generated.resources.logout
import com.rfcoding.chat.presentation.chat_list.components.ChatListHeader
import com.rfcoding.chat.presentation.chat_list.components.ChatListItem
import com.rfcoding.chat.presentation.chat_list.components.EmptyChatSection
import com.rfcoding.chat.presentation.model.ChatUi
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.designsystem.components.brand.ChirpHorizontalDivider
import com.rfcoding.core.designsystem.components.buttons.ChirpFloatingActionButton
import com.rfcoding.core.designsystem.components.dialogs.DestructiveConfirmationDialog
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatListRoot(
    onCreateChatClick: () -> Unit,
    onProfileSettingsClick: () -> Unit,
    onConfirmLogoutClick: () -> Unit,
    onChatClick: (ChatUi) -> Unit,
    viewModel: ChatListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ChatListScreen(
        state = state,
        onAction = { action ->
            when (action) {
                ChatListAction.OnCreateChatClick -> onCreateChatClick()
                ChatListAction.OnProfileSettingsClick -> onProfileSettingsClick()
                ChatListAction.OnConfirmLogout -> onConfirmLogoutClick()
                is ChatListAction.OnChatClick -> onChatClick(action.chat)
                else -> Unit
            }
            viewModel.onAction(action)
        },
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun ChatListScreen(
    state: ChatListState,
    onAction: (ChatListAction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
        floatingActionButton = {
            ChirpFloatingActionButton(
                onClick = {
                    onAction(ChatListAction.OnCreateChatClick)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.create_chat)
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ChatListHeader(
                localParticipant = state.localParticipant,
                isMenuOpen = state.isUserMenuOpen,
                onUserAvatarClick = {
                    onAction(ChatListAction.OnUserAvatarClick)
                },
                onDismissMenu = {
                    onAction(ChatListAction.OnDismissUserMenu)
                },
                onProfileSettingsClick = {
                    onAction(ChatListAction.OnProfileSettingsClick)
                },
                onLogoutClick = {
                    onAction(ChatListAction.OnLogoutClick)
                }
            )

            when {
                state.isLoadingChats -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                state.chats.isEmpty() -> {
                    EmptyChatSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(8.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(
                            items = state.chats,
                            key = { it.id }
                        ) { chat ->
                            ChatListItem(
                                chat = chat,
                                isSelected = state.selectedChatId == chat.id,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAction(ChatListAction.OnChatClick(chat))
                                    }
                            )
                            ChirpHorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    if (state.showLogoutConfirmation) {
        DestructiveConfirmationDialog(
            title = stringResource(Res.string.dialog_logout_title),
            description = stringResource(Res.string.dialog_logout_description),
            confirmButtonText = stringResource(Res.string.logout),
            cancelButtonText = stringResource(Res.string.cancel),
            onConfirmClick = {
                onAction(ChatListAction.OnConfirmLogout)
            },
            onCancelClick = {
                onAction(ChatListAction.OnDismissLogoutDialog)
            },
            onDismiss = {
                onAction(ChatListAction.OnDismissLogoutDialog)
            }
        )
    }
}

@Composable
@Preview
private fun ChatListScreenPreview() {
    ChirpTheme {
        val localParticipant = ChatParticipantUi(
            "1",
            username = "chinley1",
            initial = "CH",
            imageUrl = null
        )

        val otherParticipants = listOf(
            ChatParticipantUi(
                "2",
                username = "john",
                initial = "JO",
                imageUrl = null
            ),
            ChatParticipantUi(
                "3",
                username = "dexter",
                initial = "DE",
                imageUrl = null
            )
        )

        val chats = listOf(
            ChatUi(
                id = "1",
                localParticipant = localParticipant,
                participants = otherParticipants,
                lastMessage = null,
                lastMessageUsername = null,
                affectedUsernamesForEvent = listOf(),
                isGroupChat = true,
                name = null
            )
        )

        ChatListScreen(
            state = ChatListState(
                localParticipant = localParticipant,
                showLogoutConfirmation = false,
                isLoadingChats = false,
                chats = emptyList()
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}