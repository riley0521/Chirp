package com.rfcoding.chat.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rfcoding.chat.presentation.model.ChatUi
import com.rfcoding.core.designsystem.components.avatar.ChirpAvatarPhoto
import com.rfcoding.core.designsystem.components.avatar.ChirpStackedAvatars
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.designsystem.theme.titleXSmall

@Composable
fun ChatItemHeaderRow(
    chat: ChatUi,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
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
                displayText = otherParticipant?.initial ?: "??",
                imageUrl = otherParticipant?.imageUrl
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
}