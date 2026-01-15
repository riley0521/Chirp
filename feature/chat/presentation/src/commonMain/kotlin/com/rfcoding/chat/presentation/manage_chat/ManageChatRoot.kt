package com.rfcoding.chat.presentation.manage_chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.chat_members
import chirp.feature.chat.presentation.generated.resources.manage_chat
import chirp.feature.chat.presentation.generated.resources.save
import com.rfcoding.chat.presentation.components.manage_chat.ManageChatAction
import com.rfcoding.chat.presentation.components.manage_chat.ManageChatScreen
import com.rfcoding.chat.presentation.components.manage_chat.ManageChatState
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.designsystem.components.dialogs.ChirpAdaptiveDialogSheetLayout
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ManageChatRoot(
    chatId: String?,
    onDismiss: () -> Unit,
    onChatMembersModified: () -> Unit,
    viewModel: ManageChatViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ManageChatEvent.OnChatMembersModified -> onChatMembersModified()
        }
    }

    LaunchedEffect(chatId) {
        viewModel.onAction(ManageChatAction.OnChatSelect(chatId))
    }

    ChirpAdaptiveDialogSheetLayout(
        onDismiss = onDismiss
    ) {
        ManageChatScreen(
            titleText = if (state.isCreator) {
                stringResource(Res.string.manage_chat)
            } else stringResource(Res.string.chat_members),
            primaryButtonText = stringResource(Res.string.save),
            isCreator = state.isCreator,
            state = state,
            onAction = { action ->
                when (action) {
                    ManageChatAction.OnDismissDialog -> onDismiss()
                    else -> Unit
                }
                viewModel.onAction(action)
            }
        )
    }
}

@Preview
@Composable
fun ManageChatPreview() {
    ChirpTheme {
        val searchResult = ChatParticipantUi(
            id = "1",
            username = "adam",
            initial = "AD"
        )
        val others = listOf(
            ChatParticipantUi(
                id = "2",
                username = "Cid",
                initial = "CI"
            ),
            ChatParticipantUi(
                id = "3",
                username = "Dexter",
                initial = "DE"
            )
        )

        ManageChatScreen(
            titleText = "Manage Chat",
            primaryButtonText = "Save",
            isCreator = false,
            state = ManageChatState(
                currentSearchResult = searchResult,
                existingChatParticipants = others,
                selectedChatParticipants = listOf(searchResult)
            ),
            onAction = {}
        )
    }
}