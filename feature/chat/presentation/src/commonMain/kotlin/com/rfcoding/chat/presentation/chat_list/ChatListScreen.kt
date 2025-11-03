package com.rfcoding.chat.presentation.chat_list

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.create_chat
import com.rfcoding.chat.presentation.chat_list.components.ChatListHeader
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.designsystem.components.buttons.ChirpFloatingActionButton
import com.rfcoding.core.designsystem.components.dialogs.ChirpDialogContent
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatListRoot(
    onCreateChatClick: () -> Unit,
    onProfileSettingsClick: () -> Unit,
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
        if (state.localParticipant != null) {
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
        }
        if (state.chats.isNotEmpty()) {

        } else {

        }
    }

    if (state.showLogoutConfirmation) {
        ChirpDialogContent(
            onDismiss = {
                onAction(ChatListAction.OnDismissLogoutDialog)
            }
        ) {

        }
    }
}

@Composable
@Preview
private fun ChatListScreenPreview() {
    ChirpTheme {
        ChatListScreen(
            state = ChatListState(
                localParticipant = ChatParticipantUi(
                    "1",
                    username = "chinley1",
                    initial = "CH",
                    imageUrl = null
                ),
                showLogoutConfirmation = false
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}