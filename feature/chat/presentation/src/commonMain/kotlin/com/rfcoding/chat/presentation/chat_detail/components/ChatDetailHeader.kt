package com.rfcoding.chat.presentation.chat_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chirp.core.designsystem.generated.resources.arrow_left_icon
import chirp.core.designsystem.generated.resources.dots_icon
import chirp.core.designsystem.generated.resources.log_out_icon
import chirp.core.designsystem.generated.resources.users_icon
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.back
import chirp.feature.chat.presentation.generated.resources.chat_members
import chirp.feature.chat.presentation.generated.resources.chat_options
import chirp.feature.chat.presentation.generated.resources.leave_chat
import chirp.feature.chat.presentation.generated.resources.manage_chat
import com.rfcoding.chat.presentation.components.ChatHeader
import com.rfcoding.chat.presentation.components.ChatItemHeaderRow
import com.rfcoding.chat.presentation.model.ChatUi
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.designsystem.components.buttons.ChirpIconButton
import com.rfcoding.core.designsystem.components.dropdown.ChirpDropDownItem
import com.rfcoding.core.designsystem.components.dropdown.ChirpDropDownMenu
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import chirp.core.designsystem.generated.resources.Res as DesignSystemRes

@Composable
fun ChatDetailHeader(
    chatUi: ChatUi,
    isDetailPresent: Boolean,
    isChatOptionsDropDownOpen: Boolean,
    onBackClick: () -> Unit,
    onChatOptionsClick: () -> Unit,
    onDismissChatOptions: () -> Unit,
    onManageChatClick: () -> Unit,
    onLeaveChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!isDetailPresent) {
            ChirpIconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = vectorResource(DesignSystemRes.drawable.arrow_left_icon),
                    contentDescription = stringResource(Res.string.back),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.extended.textSecondary
                )
            }
        }

        ChatItemHeaderRow(
            chat = chatUi,
            modifier = Modifier
                .weight(1f)
        )

        Box {
            ChirpIconButton(
                onClick = onChatOptionsClick
            ) {
                Icon(
                    imageVector = vectorResource(DesignSystemRes.drawable.dots_icon),
                    contentDescription = stringResource(Res.string.chat_options),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.extended.textSecondary
                )
            }

            ChirpDropDownMenu(
                isOpen = isChatOptionsDropDownOpen,
                onDismiss = onDismissChatOptions,
                items = buildList {
                    val chatStr = stringResource(
                        if (chatUi.isCreator) {
                            Res.string.manage_chat
                        } else Res.string.chat_members
                    )
                    add(
                        ChirpDropDownItem(
                            title = chatStr,
                            icon = vectorResource(DesignSystemRes.drawable.users_icon),
                            contentColor = MaterialTheme.colorScheme.extended.textSecondary,
                            onClick = onManageChatClick
                        )
                    )
                    add(
                        ChirpDropDownItem(
                            title = stringResource(Res.string.leave_chat),
                            icon = vectorResource(DesignSystemRes.drawable.log_out_icon),
                            contentColor = MaterialTheme.colorScheme.extended.destructiveHover,
                            onClick = onLeaveChatClick
                        )
                    )
                }
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ChatDetailHeaderPreview() {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            ChatHeader {
                ChatDetailHeader(
                    chatUi = ChatUi(
                        id = "1",
                        localParticipant = localParticipant,
                        participants = otherParticipants,
                        lastMessage = null,
                        lastMessageUsername = null,
                        affectedUsernamesForEvent = listOf(),
                        isGroupChat = true,
                        creatorId = "1",
                        name = null
                    ),
                    isDetailPresent = false,
                    isChatOptionsDropDownOpen = true,
                    onBackClick = {},
                    onChatOptionsClick = {},
                    onDismissChatOptions = {},
                    onManageChatClick = {},
                    onLeaveChatClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

    }
}