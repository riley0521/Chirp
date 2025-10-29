package com.rfcoding.chat.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.designsystem.components.avatar.ChirpAvatarPhoto
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.designsystem.theme.titleXSmall
import com.rfcoding.core.presentation.util.DeviceConfiguration
import com.rfcoding.core.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ColumnScope.ChatParticipantSelectionSection(
    selectedParticipants: List<ChatParticipantUi>,
    modifier: Modifier = Modifier,
    searchResult: ChatParticipantUi? = null
) {
    val configuration = currentDeviceConfiguration()
    val rootHeightModifier = when (configuration) {
        DeviceConfiguration.TABLET_PORTRAIT,
        DeviceConfiguration.TABLET_LANDSCAPE,
        DeviceConfiguration.DESKTOP -> {
            Modifier
                .animateContentSize()
                .heightIn(min = 200.dp, max = 300.dp)
        }
        else -> Modifier
            .weight(1f)
    }

    Box(
        modifier = rootHeightModifier
            .then(modifier)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            searchResult?.let {
                item {
                    ChatParticipantListItem(
                        participant = it,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (selectedParticipants.isNotEmpty() && searchResult == null) {
                items(
                    items = selectedParticipants,
                    key = { it.id }
                ) { participant ->
                    ChatParticipantListItem(
                        participant = participant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ChatParticipantListItem(
    participant: ChatParticipantUi,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ChirpAvatarPhoto(
            displayText = participant.initial,
            imageUrl = participant.imageUrl
        )
        Text(
            text = participant.username,
            style = MaterialTheme.typography.titleXSmall,
            color = MaterialTheme.colorScheme.extended.textPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
@Preview
private fun ChatParticipantSelectionSectionPreview() {
    ChirpTheme {
        val selectedParticipants = mutableListOf<ChatParticipantUi>()
        repeat(3) {
            selectedParticipants.add(
                ChatParticipantUi(
                    id = it.toString(),
                    username = "chinley$it",
                    initial = "CC"
                )
            )
        }
        val searchResult = ChatParticipantUi(
            id = "5",
            username = "rfcutie",
            initial = "RF"
        )

        Column {
            ChatParticipantSelectionSection(
                selectedParticipants = selectedParticipants,
                searchResult = null
            )
        }
    }
}