package com.rfcoding.chat.presentation.chat_list

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.cancel
import chirp.feature.chat.presentation.generated.resources.create_chat
import chirp.feature.chat.presentation.generated.resources.dialog_logout_description
import chirp.feature.chat.presentation.generated.resources.dialog_logout_title
import chirp.feature.chat.presentation.generated.resources.logout
import chirp.feature.chat.presentation.generated.resources.no_chats
import chirp.feature.chat.presentation.generated.resources.no_chats_subtitle
import com.rfcoding.chat.presentation.chat_list.components.ChatListHeader
import com.rfcoding.chat.presentation.chat_list.components.ChatListItem
import com.rfcoding.chat.presentation.components.EmptyListSection
import com.rfcoding.chat.presentation.model.ChatUi
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.designsystem.components.brand.ChirpHorizontalDivider
import com.rfcoding.core.designsystem.components.buttons.ChirpFloatingActionButton
import com.rfcoding.core.designsystem.components.dialogs.DestructiveConfirmationDialog
import com.rfcoding.core.designsystem.components.others.ChirpPullToRefreshBox
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.presentation.permissions.Permission
import com.rfcoding.core.presentation.permissions.rememberPermissionController
import com.rfcoding.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatListRoot(
    selectedChatId: String?,
    onCreateChatClick: () -> Unit,
    onProfileSettingsClick: () -> Unit,
    onConfirmLogoutClick: () -> Unit,
    onChatClick: (String?) -> Unit,
    viewModel: ChatListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(selectedChatId) {
        viewModel.onAction(ChatListAction.OnSelectChat(null))
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ChatListEvent.Error -> {
                snackbarHostState.showSnackbar(event.error.asStringAsync())
            }
            ChatListEvent.OnSuccessfulLogout -> {
                onConfirmLogoutClick()
            }
        }
    }

    ChatListScreen(
        state = state,
        onAction = { action ->
            when (action) {
                ChatListAction.OnCreateChatClick -> onCreateChatClick()
                ChatListAction.OnProfileSettingsClick -> onProfileSettingsClick()
                is ChatListAction.OnSelectChat -> onChatClick(action.chatId)
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
    val permissionController = rememberPermissionController()
    LaunchedEffect(true) {
        permissionController.requestPermission(Permission.NOTIFICATION)
    }

    var headerHeight by remember {
        mutableStateOf(0.dp)
    }
    val density = LocalDensity.current

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
        ChirpPullToRefreshBox(
            isRefreshing = state.isLoadingChats,
            modifier = Modifier.padding(innerPadding),
            topPadding = headerHeight
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
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
                    },
                    modifier = Modifier.onSizeChanged { size ->
                        headerHeight = with(density) { size.height.toDp() }
                    }
                )

                when {
                    state.chats.isEmpty() -> {
                        EmptyListSection(
                            title = stringResource(Res.string.no_chats),
                            description = stringResource(Res.string.no_chats_subtitle),
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
                                            onAction(ChatListAction.OnSelectChat(chat.id))
                                        }
                                )
                                ChirpHorizontalDivider()
                            }
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
            },
            isConfirmButtonLoading = state.isLoggingOut
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
                creatorId = "1",
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