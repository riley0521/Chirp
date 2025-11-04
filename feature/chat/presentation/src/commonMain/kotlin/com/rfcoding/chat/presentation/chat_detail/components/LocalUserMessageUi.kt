package com.rfcoding.chat.presentation.chat_detail.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import chirp.core.designsystem.generated.resources.reload_icon
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.delete_for_everyone
import chirp.feature.chat.presentation.generated.resources.retry
import chirp.feature.chat.presentation.generated.resources.you
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.presentation.model.MessageUi
import com.rfcoding.core.designsystem.components.chat.ChirpChatBubble
import com.rfcoding.core.designsystem.components.chat.TrianglePosition
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.presentation.util.UiText
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import chirp.core.designsystem.generated.resources.Res as DesignSystemRes

@Composable
fun LocalUserMessageUi(
    message: MessageUi.LocalUserMessage,
    onMessageLongClick: () -> Unit,
    onDismissMessageMenu: () -> Unit,
    onDeleteClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        Box(
            modifier = Modifier.weight(1f)
        ) {
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
                }
            )

            DropdownMenu(
                expanded = message.isMenuOpen,
                onDismissRequest = onDismissMessageMenu,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.extended.surfaceOutline
                )
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(Res.string.delete_for_everyone),
                            color = MaterialTheme.colorScheme.extended.destructiveHover,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        onDismissMessageMenu()
                        onDeleteClick()
                    }
                )
            }
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
            isMenuOpen = true,
            formattedSentTime = UiText.DynamicText("Friday 6:45 PM")
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LocalUserMessageUi(
                message = message,
                onMessageLongClick = {},
                onDismissMessageMenu = {},
                onDeleteClick = {},
                onRetryClick = {},
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
            isMenuOpen = true,
            formattedSentTime = UiText.DynamicText("Friday 6:45 PM")
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LocalUserMessageUi(
                message = message,
                onMessageLongClick = {},
                onDismissMessageMenu = {},
                onDeleteClick = {},
                onRetryClick = {},
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}