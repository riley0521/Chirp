package com.rfcoding.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.account_deleted
import com.rfcoding.chat.domain.models.ChatMessageType
import com.rfcoding.chat.presentation.chat_detail.VoiceMessageState
import com.rfcoding.chat.presentation.model.MediaUi
import com.rfcoding.chat.presentation.model.MessageUi
import com.rfcoding.chat.presentation.util.getChatBubbleColorForUser
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.designsystem.components.avatar.ChirpAvatarPhoto
import com.rfcoding.core.designsystem.components.chat.ChirpChatBubble
import com.rfcoding.core.designsystem.components.chat.TrianglePosition
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.presentation.util.UiText
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun OtherUserMessageUi(
    message: MessageUi.OtherUserMessage,
    voiceMessageState: VoiceMessageState,
    onTogglePlayback: () -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChirpAvatarPhoto(
            displayText = message.sender?.initial ?: "??",
            imageUrl = message.sender?.imageUrl
        )
        ChirpChatBubble(
            messageContent = message.content,
            sender = message.sender?.username ?: stringResource(Res.string.account_deleted),
            formattedDateTime = message.formattedSentTime.asString(),
            trianglePosition = TrianglePosition.LEFT,
            voiceChatUi = if (message.messageType == ChatMessageType.MESSAGE_VOICE_OVER_ONLY) {
                {
                    val state = remember(voiceMessageState) {
                        if (voiceMessageState.selectedAudio == message.content) {
                            voiceMessageState
                        } else {
                            null
                        }
                    }

                    ChatVoiceMessagePlayer(
                        totalDuration = message.audioDurationInSeconds.seconds,
                        durationPlayed = state?.durationPlayed ?: Duration.ZERO,
                        hasStarted = state != null,
                        isPlaying = state?.isPlaying == true,
                        isBuffering = state?.isBuffering == true,
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
            },
            color = getChatBubbleColorForUser(message.sender?.id)
        )
    }
}

@Composable
@Preview
private fun OtherUserMessageUiPreview() {
    ChirpTheme {
        val message = MessageUi.OtherUserMessage(
            id = "1",
            content = "Hello again world! This will be a long message that hopefully spans multiple lines.",
            sender = ChatParticipantUi(
                id = "1",
                username = "john",
                initial = "JO",
                imageUrl = null
            ),
            formattedSentTime = UiText.DynamicText("Friday 6:44 PM"),
            media = MediaUi.NoMedia
        )

        OtherUserMessageUi(
            message = message,
            voiceMessageState = VoiceMessageState(),
            onTogglePlayback = {},
            onImageClick = {},
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}