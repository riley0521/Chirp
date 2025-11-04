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
import androidx.compose.ui.unit.dp
import com.rfcoding.chat.presentation.model.MessageUi
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.presentation.util.UiText
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MessageListItem(
    message: MessageUi,
    onMessageLongClick: () -> Unit,
    onDismissMessageMenu: () -> Unit,
    onDeleteClick: () -> Unit,
    onRetryClick: () -> Unit,
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
                    onMessageLongClick = onMessageLongClick,
                    onDismissMessageMenu = onDismissMessageMenu,
                    onDeleteClick = onDeleteClick,
                    onRetryClick = onRetryClick
                )
            }
            is MessageUi.OtherUserMessage -> {
                OtherUserMessageUi(message = message)
            }
            is MessageUi.EventMessage -> Unit
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
private fun MessageListItemPreview() {
    ChirpTheme {
        val dateSeparator = MessageUi.DateSeparator(
            id = "1",
            date = UiText.DynamicText("Today")
        )

        MessageListItem(
            message = dateSeparator,
            onMessageLongClick = {},
            onDismissMessageMenu = {},
            onDeleteClick = {},
            onRetryClick = {}
        )
    }
}