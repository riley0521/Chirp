package com.rfcoding.chat.presentation.chat_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.account_deleted
import com.rfcoding.chat.domain.models.ChatMessage
import com.rfcoding.chat.domain.models.ChatMessageEvent
import com.rfcoding.chat.domain.models.ChatMessageEventType
import com.rfcoding.chat.domain.models.ChatMessageType
import com.rfcoding.chat.presentation.model.ChatUi
import com.rfcoding.chat.presentation.util.getLastMessageContent
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.designsystem.components.avatar.ChirpAvatarPhoto
import com.rfcoding.core.designsystem.components.avatar.ChirpStackedAvatars
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.designsystem.theme.titleXSmall
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock

@Composable
fun ChatListItem(
    chat: ChatUi,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.extended.surfaceLower
                }
            )
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (chat.isGroupChat) {
                    ChirpStackedAvatars(
                        avatars = chat.participants
                    )
                } else {
                    val otherParticipant = chat.participants.first()

                    ChirpAvatarPhoto(
                        displayText = otherParticipant.initial,
                        imageUrl = otherParticipant.imageUrl
                    )
                }
                Text(
                    text = chat.chatName,
                    style = MaterialTheme.typography.titleXSmall,
                    color = MaterialTheme.colorScheme.extended.textPrimary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (chat.lastMessage != null) {
                getLastMessageContent(
                    message = chat.lastMessage,
                    username = chat.lastMessageUsername ?: stringResource(Res.string.account_deleted),
                    affectedUsernames = chat.affectedUsernamesForEvent
                )?.asStyledText()?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.extended.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .alpha(if (isSelected) 1f else 0f)
                .background(MaterialTheme.colorScheme.primary)
                .width(4.dp)
                .fillMaxHeight()
        )
    }
}

@Composable
@Preview
private fun ChatListItemPreview() {
    ChirpTheme {
        val participants = listOf(
            ChatParticipantUi(
                id = "u1",
                username = "chinley1",
                initial = "CC"
            ),
            ChatParticipantUi(
                id = "u2",
                username = "rfcutie1",
                initial = "RF"
            ),
            ChatParticipantUi(
                id = "u3",
                username = "afcutie1",
                initial = "AF"
            ),
            ChatParticipantUi(
                id = "u4",
                username = "bfcutie1",
                initial = "BF"
            ),
            ChatParticipantUi(
                id = "u5",
                username = "dfcutie1",
                initial = "DF"
            ),
            ChatParticipantUi(
                id = "u6",
                username = "dfcutie2",
                initial = "DF"
            )
        )
        val localUser = participants[1]
        val lastMessage = ChatMessage(
            id = "m1",
            chatId = "chat1",
            senderId = "u1",
            content = "Hello World World World World World World World World World World World World World World World!",
            messageType = ChatMessageType.MESSAGE_TEXT,
            imageUrls = emptyList(),
            event = ChatMessageEvent(
                affectedUserIds = listOf("u6"),
                type = ChatMessageEventType.PARTICIPANTS_ADDED
            ),
            createdAt = Clock.System.now()
        )

        ChatListItem(
            chat = ChatUi(
                id = "abc1",
                localParticipant = localUser,
                participants = participants - localUser,
                lastMessage = lastMessage,
                lastMessageUsername = "chinley1",
                affectedUsernamesForEvent = listOf("dfcutie2"),
                isGroupChat = true,
                name = null
            ),
            isSelected = false
        )
    }
}