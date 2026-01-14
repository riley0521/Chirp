package com.rfcoding.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.account_deleted
import com.rfcoding.chat.domain.models.ChatMessageEventType
import com.rfcoding.chat.presentation.chat_detail.VoiceMessageState
import com.rfcoding.chat.presentation.model.MessageUi
import com.rfcoding.chat.presentation.util.getDescriptiveMessageEvent
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.presentation.util.UiText
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MessageListItem(
    message: MessageUi,
    voiceMessageState: VoiceMessageState,
    onTogglePlayback: () -> Unit,
    messageWithOpenMenu: MessageUi.LocalUserMessage?,
    onMessageLongClick: (MessageUi.LocalUserMessage) -> Unit,
    onDismissMessageMenu: () -> Unit,
    onDeleteClick: (MessageUi.LocalUserMessage) -> Unit,
    onRetryClick: (MessageUi.LocalUserMessage) -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        when (message) {
            is MessageUi.DateSeparator -> {
                DateSeparatorUi(
                    date = message.date.asString(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            is MessageUi.LocalUserMessage -> {
                LocalUserMessageUi(
                    message = message,
                    voiceMessageState = voiceMessageState,
                    onTogglePlayback = onTogglePlayback,
                    messageWithOpenMenu = messageWithOpenMenu,
                    onMessageLongClick = {
                        onMessageLongClick(message)
                    },
                    onDismissMessageMenu = onDismissMessageMenu,
                    onDeleteClick = {
                        onDeleteClick(message)
                    },
                    onRetryClick = {
                        onRetryClick(message)
                    },
                    onImageClick = onImageClick
                )
            }
            is MessageUi.OtherUserMessage -> {
                OtherUserMessageUi(
                    message = message,
                    voiceMessageState = voiceMessageState,
                    onTogglePlayback = onTogglePlayback,
                    onImageClick = onImageClick
                )
            }
            is MessageUi.EventMessage -> {
                val descriptiveMessage = getDescriptiveMessageEvent(
                    type = message.type,
                    username = message.username ?: stringResource(Res.string.account_deleted),
                    affectedUsernames = message.affectedUsernames
                )

                Text(
                    text = descriptiveMessage.asString(),
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.extended.textSecondary
                )
            }
        }
    }
}


@Composable
private fun DateSeparatorUi(
    date: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = date,
            modifier = Modifier
                .padding(horizontal = 40.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.extended.textPlaceholder
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
@Preview(showBackground = true)
private fun DateSeparatorItemPreview() {
    ChirpTheme {
        val dateSeparator = MessageUi.DateSeparator(
            id = "1",
            date = UiText.DynamicText("Today")
        )

        MessageListItem(
            message = dateSeparator,
            voiceMessageState = VoiceMessageState(),
            onTogglePlayback = {},
            messageWithOpenMenu = null,
            onMessageLongClick = {},
            onDismissMessageMenu = {},
            onDeleteClick = {},
            onRetryClick = {},
            onImageClick = {}
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun ParticipantsAddedEventItemPreview() {
    ChirpTheme {
        val item = MessageUi.EventMessage(
            id = "1",
            type = ChatMessageEventType.PARTICIPANTS_ADDED,
            username = "chinley1",
            affectedUsernames = listOf("chinley2", "chinley3")
        )

        MessageListItem(
            message = item,
            voiceMessageState = VoiceMessageState(),
            onTogglePlayback = {},
            messageWithOpenMenu = null,
            onMessageLongClick = {},
            onDismissMessageMenu = {},
            onDeleteClick = {},
            onRetryClick = {},
            onImageClick = {}
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun ParticipantRemovedEventItemPreview() {
    ChirpTheme {
        val item = MessageUi.EventMessage(
            id = "1",
            type = ChatMessageEventType.PARTICIPANT_REMOVED_BY_CREATOR,
            username = "chinley1",
            affectedUsernames = listOf("chinley2")
        )

        MessageListItem(
            message = item,
            voiceMessageState = VoiceMessageState(),
            onTogglePlayback = {},
            messageWithOpenMenu = null,
            onMessageLongClick = {},
            onDismissMessageMenu = {},
            onDeleteClick = {},
            onRetryClick = {},
            onImageClick = {}
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun ParticipantsLeftChatEventItemPreview() {
    ChirpTheme {
        val item = MessageUi.EventMessage(
            id = "1",
            type = ChatMessageEventType.PARTICIPANT_LEFT,
            username = "chinley2",
            affectedUsernames = listOf()
        )

        MessageListItem(
            message = item,
            voiceMessageState = VoiceMessageState(),
            onTogglePlayback = {},
            messageWithOpenMenu = null,
            onMessageLongClick = {},
            onDismissMessageMenu = {},
            onDeleteClick = {},
            onRetryClick = {},
            onImageClick = {}
        )
    }
}