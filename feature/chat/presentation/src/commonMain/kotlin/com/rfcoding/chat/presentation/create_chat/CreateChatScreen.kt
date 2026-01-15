package com.rfcoding.chat.presentation.create_chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.create_chat
import com.rfcoding.chat.domain.models.Chat
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
fun CreateChatRoot(
    onDismiss: () -> Unit,
    onChatCreated: (Chat) -> Unit,
    viewModel: CreateChatViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is CreateChatEvent.OnChatCreated -> onChatCreated(event.chat)
        }
    }

    ChirpAdaptiveDialogSheetLayout(
        onDismiss = onDismiss
    ) {
        ManageChatScreen(
            titleText = stringResource(Res.string.create_chat),
            primaryButtonText = stringResource(Res.string.create_chat),
            isCreator = true,
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
private fun Preview() {
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
            titleText = "Create Chat",
            primaryButtonText = "Create Chat",
            isCreator = true,
            state = ManageChatState(
                currentSearchResult = searchResult,
                existingChatParticipants = others,
                //selectedChatParticipants = others,
            ),
            onAction = {}
        )
    }
}