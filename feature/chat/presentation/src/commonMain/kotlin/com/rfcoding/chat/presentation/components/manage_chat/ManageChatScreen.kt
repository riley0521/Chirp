package com.rfcoding.chat.presentation.components.manage_chat

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
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.cancel
import com.rfcoding.chat.presentation.components.ChatParticipantListItem
import com.rfcoding.chat.presentation.components.ChatParticipantSearchTextSection
import com.rfcoding.chat.presentation.components.ChatParticipantSelectionSection
import com.rfcoding.chat.presentation.components.ManageChatButtonSection
import com.rfcoding.chat.presentation.components.ManageChatHeaderRow
import com.rfcoding.core.designsystem.components.brand.ChirpHorizontalDivider
import com.rfcoding.core.designsystem.components.buttons.ChirpButton
import com.rfcoding.core.designsystem.components.buttons.ChirpButtonStyle
import com.rfcoding.core.presentation.util.DeviceConfiguration
import com.rfcoding.core.presentation.util.clearFocusOnTap
import com.rfcoding.core.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.resources.stringResource

@Composable
fun ManageChatScreen(
    titleText: String,
    primaryButtonText: String,
    isCreator: Boolean,
    state: ManageChatState,
    onAction: (ManageChatAction) -> Unit,
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
                title = titleText,
                onCloseClick = {
                    onAction(ManageChatAction.OnDismissDialog)
                },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        ChirpHorizontalDivider()
        if (isCreator) {
            ChatParticipantSearchTextSection(
                queryState = state.queryTextFieldState,
                onAddClick = {
                    onAction(ManageChatAction.OnAddClick)
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
            state.currentSearchResult?.let {
                ChatParticipantListItem(
                    participant = it,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            ChirpHorizontalDivider()
        }
        ChatParticipantSelectionSection(
            existingParticipants = state.existingChatParticipants,
            selectedParticipants = if (isCreator) {
                state.selectedChatParticipants
            } else emptyList()
        )
        if (isCreator) {
            ChirpHorizontalDivider()
            ManageChatButtonSection(
                primaryButton = {
                    ChirpButton(
                        text = primaryButtonText,
                        onClick = {
                            onAction(ManageChatAction.OnCreateChatClick)
                        },
                        enabled = state
                            .selectedChatParticipants
                            .isNotEmpty() && !state.isCreatingChat,
                        isLoading = state.isCreatingChat
                    )
                },
                secondaryButton = {
                    ChirpButton(
                        text = stringResource(Res.string.cancel),
                        onClick = {
                            onAction(ManageChatAction.OnDismissDialog)
                        },
                        style = ChirpButtonStyle.SECONDARY
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}