package com.rfcoding.chat.presentation.chat_list_detail

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfcoding.chat.presentation.chat_detail.ChatDetailRoot
import com.rfcoding.chat.presentation.chat_list.ChatListRoot
import com.rfcoding.chat.presentation.create_chat.CreateChatRoot
import com.rfcoding.chat.presentation.manage_chat.ManageChatRoot
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.presentation.util.DialogSheetScopedViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun ChatListDetailAdaptiveLayout(
    onConfirmLogout: () -> Unit,
    viewModel: ChatListDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val scaffoldDirective = createNoSpacingPaneScaffoldDirective()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator(
        scaffoldDirective = scaffoldDirective
    )
    val scope = rememberCoroutineScope()

    BackHandler(enabled = scaffoldNavigator.canNavigateBack()) {
        scope.launch {
            scaffoldNavigator.navigateBack()
        }
    }

    val detailPane = scaffoldNavigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail]
    LaunchedEffect(detailPane, state.selectedChatId) {
        if (detailPane == PaneAdaptedValue.Hidden && state.selectedChatId != null) {
            viewModel.onAction(ChatListDetailAction.OnChatClick(null))
        }
    }

    ListDetailPaneScaffold(
        directive = scaffoldDirective,
        value = scaffoldNavigator.scaffoldValue,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.extended.surfaceLower),
        listPane = {
            AnimatedPane {
                ChatListRoot(
                    onCreateChatClick = {
                        viewModel.onAction(ChatListDetailAction.OnCreateChatClick)
                    },
                    onProfileSettingsClick = {
                        viewModel.onAction(ChatListDetailAction.OnProfileSettingsClick)
                    },
                    onConfirmLogoutClick = onConfirmLogout,
                    onChatClick = {
                        viewModel.onAction(ChatListDetailAction.OnChatClick(it.id))
                        scope.launch {
                            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                        }
                    }
                )
            }
        },
        detailPane = {
            AnimatedPane {
                val listPane = scaffoldNavigator.scaffoldValue[ListDetailPaneScaffoldRole.List]

                ChatDetailRoot(
                    chatId = state.selectedChatId,
                    isDetailPresent = detailPane == PaneAdaptedValue.Expanded && listPane == PaneAdaptedValue.Expanded,
                    onBack = {
                        if (scaffoldNavigator.canNavigateBack()) {
                            scope.launch { scaffoldNavigator.navigateBack() }
                        }
                    },
                    onViewChatMembersOrManageChatClick = {
                        viewModel.onAction(ChatListDetailAction.OnManageChatClick)
                    }
                )
            }
        }
    )

    DialogSheetScopedViewModel(
        visible = state.dialogState is DialogState.CreateChat
    ) {
        CreateChatRoot(
            onDismiss = {
                viewModel.onAction(ChatListDetailAction.OnDismissCurrentDialog)
            },
            onChatCreated = { chat ->
                viewModel.onAction(ChatListDetailAction.OnDismissCurrentDialog)
                viewModel.onAction(ChatListDetailAction.OnChatClick(chat.id))
                scope.launch {
                    scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                }
            }
        )
    }

    DialogSheetScopedViewModel(
        visible = state.dialogState is DialogState.ManageChat
    ) {
        val chatId = (state.dialogState as? DialogState.ManageChat)?.chatId ?: return@DialogSheetScopedViewModel

        ManageChatRoot(
            chatId = chatId,
            onDismiss = {
                viewModel.onAction(ChatListDetailAction.OnDismissCurrentDialog)
            },
            onChatMembersModified = {
                viewModel.onAction(ChatListDetailAction.OnDismissCurrentDialog)
            }
        )
    }
}

@Composable
@Preview
private fun ChatListDetailAdaptiveLayoutPreview() {
    ChirpTheme {
        ChatListDetailAdaptiveLayout(
            onConfirmLogout = {}
        )
    }
}