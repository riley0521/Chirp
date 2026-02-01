package com.rfcoding.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chirp.core.designsystem.generated.resources.reload_icon
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.delete_for_everyone
import chirp.feature.chat.presentation.generated.resources.retry
import chirp.feature.chat.presentation.generated.resources.you
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.domain.models.ChatMessageType
import com.rfcoding.chat.presentation.chat_detail.VoiceMessageState
import com.rfcoding.chat.presentation.model.MediaUi
import com.rfcoding.chat.presentation.model.MessageUi
import com.rfcoding.core.designsystem.components.chat.ChirpChatBubble
import com.rfcoding.core.designsystem.components.chat.TrianglePosition
import com.rfcoding.core.designsystem.components.dropdown.ChirpDropDownItem
import com.rfcoding.core.designsystem.components.dropdown.ChirpDropDownMenu
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.presentation.util.UiText
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import chirp.core.designsystem.generated.resources.Res as DesignSystemRes

/**
 * @param onImageClick the String param is the URL or Uri of the image.
 */
@Composable
fun LocalUserMessageUi(
    message: MessageUi.LocalUserMessage,
    voiceMessageState: VoiceMessageState,
    onTogglePlayback: () -> Unit,
    messageWithOpenMenu: MessageUi.LocalUserMessage?,
    onMessageLongClick: () -> Unit,
    onDismissMessageMenu: () -> Unit,
    onDeleteClick: () -> Unit,
    onRetryClick: () -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        Box {
            ChirpChatBubble(
                messageContent = message.content,
                sender = stringResource(Res.string.you),
                formattedDateTime = message.formattedSentTime.asString(),
                trianglePosition = TrianglePosition.RIGHT,
                messageStatus = {
                    MessageStatusUi(
                        status = message.deliveryStatus
                    )
                },
                onLongClick = {
                    onMessageLongClick()
                },
                voiceChatUi = if (message.messageType == ChatMessageType.MESSAGE_VOICE_OVER_ONLY) {
                    {
                        val hasStarted = remember(voiceMessageState.selectedAudio) {
                            voiceMessageState.selectedAudio == message.content
                        }
                        val durationPlayed = if (hasStarted) {
                            voiceMessageState.durationPlayed
                        } else Duration.ZERO

                        ChatVoiceMessagePlayer(
                            totalDuration = message.audioDurationInSeconds.seconds,
                            durationPlayed = durationPlayed,
                            hasStarted = hasStarted,
                            isPlaying = voiceMessageState.isPlaying && hasStarted,
                            isBuffering = voiceMessageState.isBuffering && hasStarted,
                            onTogglePlayback = onTogglePlayback
                        )
                    }
                } else null,
                imageUIs = {
                    if (message.media is MediaUi.Images &&
                        message.messageType == ChatMessageType.MESSAGE_TEXT_WITH_IMAGES
                    ) {
                        MessageThumbnails(
                            images = message.media.images,
                            onImageClick = onImageClick,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            )

            ChirpDropDownMenu(
                isOpen = message.id == messageWithOpenMenu?.id,
                onDismiss = onDismissMessageMenu,
                 items = listOf(
                     ChirpDropDownItem(
                         title = stringResource(Res.string.delete_for_everyone),
                         icon = Icons.Default.Delete,
                         contentColor = MaterialTheme.colorScheme.extended.destructiveHover,
                         onClick = onDeleteClick
                     )
                 )
            )
        }

        if (message.deliveryStatus == ChatMessageDeliveryStatus.FAILED) {
            IconButton(
                onClick = onRetryClick
            ) {
                Icon(
                    imageVector = vectorResource(DesignSystemRes.drawable.reload_icon),
                    contentDescription = stringResource(Res.string.retry),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun LocalUserMessageUiPreview() {
    ChirpTheme {
        val message = MessageUi.LocalUserMessage(
            id = "1",
            content = "Hello world!",
            deliveryStatus = ChatMessageDeliveryStatus.SENT,
            formattedSentTime = UiText.DynamicText("Friday 6:45 PM"),
            media = MediaUi.NoMedia
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LocalUserMessageUi(
                message = message,
                voiceMessageState = VoiceMessageState(),
                onTogglePlayback = {},
                messageWithOpenMenu = null,
                onMessageLongClick = {},
                onDismissMessageMenu = {},
                onDeleteClick = {},
                onRetryClick = {},
                onImageClick = {},
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun LocalUserMessageUiRetryPreview() {
    ChirpTheme {
        val message = MessageUi.LocalUserMessage(
            id = "1",
            content = "Hello world!",
            deliveryStatus = ChatMessageDeliveryStatus.FAILED,
            formattedSentTime = UiText.DynamicText("Friday 6:45 PM"),
            media = MediaUi.NoMedia
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LocalUserMessageUi(
                message = message,
                voiceMessageState = VoiceMessageState(),
                onTogglePlayback = {},
                messageWithOpenMenu = null,
                onMessageLongClick = {},
                onDismissMessageMenu = {},
                onDeleteClick = {},
                onRetryClick = {},
                onImageClick = {},
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}