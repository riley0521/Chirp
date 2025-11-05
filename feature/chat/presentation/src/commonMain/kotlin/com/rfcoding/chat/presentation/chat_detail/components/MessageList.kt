package com.rfcoding.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.no_messages
import chirp.feature.chat.presentation.generated.resources.no_messages_subtitle
import com.rfcoding.chat.domain.models.ChatMessageDeliveryStatus
import com.rfcoding.chat.presentation.components.EmptyListSection
import com.rfcoding.chat.presentation.model.MessageUi
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.presentation.util.UiText
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MessageList(
    messages: List<MessageUi>,
    listState: LazyListState,
    onMessageLongClick: (MessageUi.LocalUserMessage) -> Unit,
    onMessageRetryClick: (MessageUi.LocalUserMessage) -> Unit,
    onDeleteMessageClick: (MessageUi.LocalUserMessage) -> Unit,
    onImageClick: (String) -> Unit,
    onDismissMessageMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (messages.isEmpty()) {
        Box(
            modifier = modifier
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            EmptyListSection(
                title = stringResource(Res.string.no_messages),
                description = stringResource(Res.string.no_messages_subtitle)
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            state = listState,
            contentPadding = PaddingValues(16.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = messages,
                key = { it.id }
            ) { message ->
                MessageListItem(
                    message = message,
                    onMessageLongClick = onMessageLongClick,
                    onDismissMessageMenu = onDismissMessageMenu,
                    onRetryClick = onMessageRetryClick,
                    onDeleteClick = onDeleteMessageClick,
                    onImageClick = onImageClick,
                    modifier = Modifier
                        .animateItem()
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun MessageListPreview() {
    ChirpTheme {
        val messages = listOf(
            MessageUi.LocalUserMessage(
                id = "1",
                content = "Hello world!",
                deliveryStatus = ChatMessageDeliveryStatus.SENT,
                isMenuOpen = false,
                formattedSentTime = UiText.DynamicText("Friday 6:45 PM")
            ),
            MessageUi.OtherUserMessage(
                "2",
                content = "Hello to you too!",
                sender = ChatParticipantUi(
                    id = "1",
                    username = "john",
                    initial = "JO"
                ),
                formattedSentTime = UiText.DynamicText("Friday 6:44 PM")
            )
        )

        MessageList(
            messages = messages,
            listState = rememberLazyListState(),
            onMessageLongClick = {},
            onMessageRetryClick = {},
            onDeleteMessageClick = {},
            onImageClick = {},
            onDismissMessageMenu = {},
            modifier = Modifier
                .fillMaxSize()
        )
    }
}