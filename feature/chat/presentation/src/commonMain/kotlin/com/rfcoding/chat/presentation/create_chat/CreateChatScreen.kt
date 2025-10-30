package com.rfcoding.chat.presentation.create_chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.cancel
import chirp.feature.chat.presentation.generated.resources.create_chat
import com.rfcoding.chat.presentation.components.ChatParticipantSearchTextSection
import com.rfcoding.chat.presentation.components.ChatParticipantSelectionSection
import com.rfcoding.chat.presentation.components.ManageChatButtonSection
import com.rfcoding.chat.presentation.components.ManageChatHeaderRow
import com.rfcoding.core.designsystem.components.brand.ChirpHorizontalDivider
import com.rfcoding.core.designsystem.components.buttons.ChirpButton
import com.rfcoding.core.designsystem.components.buttons.ChirpButtonStyle
import com.rfcoding.core.designsystem.components.dialogs.ChirpAdaptiveDialogSheetLayout
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.presentation.util.DeviceConfiguration
import com.rfcoding.core.presentation.util.clearFocusOnTap
import com.rfcoding.core.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateChatRoot(
    onDismiss: () -> Unit,
    viewModel: CreateChatViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ChirpAdaptiveDialogSheetLayout(
        onDismiss = {
            onDismiss()
        }
    ) {
        CreateChatScreen(
            state = state,
            onAction = { action ->
                when (action) {
                    CreateChatAction.OnDismissDialog -> onDismiss()
                    else -> Unit
                }

                viewModel.onAction(action)
            }
        )
    }
}

@Composable
fun CreateChatScreen(
    state: CreateChatState,
    onAction: (CreateChatAction) -> Unit,
) {
    var isTextFieldFocused by remember { mutableStateOf(false) }
    val imeHeight = WindowInsets.ime.getBottom(LocalDensity.current)
    val isKeyboardVisible = imeHeight > 0
    val configuration = currentDeviceConfiguration()

    val shouldHideHeader = configuration == DeviceConfiguration.MOBILE_LANDSCAPE
            || (isKeyboardVisible && configuration != DeviceConfiguration.DESKTOP)
            || isTextFieldFocused

    Column(
        modifier = Modifier
            .clearFocusOnTap()
            .fillMaxWidth()
            .wrapContentHeight()
            .imePadding()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
    ) {

        AnimatedVisibility(
            visible = !shouldHideHeader
        ) {
            ManageChatHeaderRow(
                title = stringResource(Res.string.create_chat),
                onCloseClick = {
                    onAction(CreateChatAction.OnDismissDialog)
                },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        ChirpHorizontalDivider()
        ChatParticipantSearchTextSection(
            queryState = state.queryTextFieldState,
            onAddClick = {
                onAction(CreateChatAction.OnAddClick)
            },
            isAddingEnabled = state.canAddParticipant,
            isLoading = state.isSearching,
            error = state.searchError,
            modifier = Modifier
                .fillMaxWidth(),
            onFocusChanged = {
                isTextFieldFocused = it
            }
        )
        ChirpHorizontalDivider()
        ChatParticipantSelectionSection(
            selectedParticipants = state.selectedChatParticipants,
            searchResult = state.currentSearchResult
        )
        ChirpHorizontalDivider()
        ManageChatButtonSection(
            primaryButton = {
                ChirpButton(
                    text = stringResource(Res.string.create_chat),
                    onClick = {
                        onAction(CreateChatAction.OnCreateChatClick)
                    },
                    enabled = state.selectedChatParticipants.isNotEmpty(),
                    isLoading = state.isCreatingChat
                )
            },
            secondaryButton = {
                ChirpButton(
                    text = stringResource(Res.string.cancel),
                    onClick = {
                        onAction(CreateChatAction.OnDismissDialog)
                    },
                    style = ChirpButtonStyle.SECONDARY
                )
            },
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun Preview() {
    ChirpTheme {
        CreateChatScreen(
            state = CreateChatState(),
            onAction = {}
        )
    }
}