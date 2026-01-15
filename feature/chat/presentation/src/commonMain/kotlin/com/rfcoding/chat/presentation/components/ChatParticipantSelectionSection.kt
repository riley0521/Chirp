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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.designsystem.components.avatar.ChirpAvatarPhoto
import com.rfcoding.core.designsystem.components.brand.ChirpHorizontalDivider
import com.rfcoding.core.designsystem.components.buttons.ChirpIconButton
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.designsystem.theme.titleXSmall
import com.rfcoding.core.presentation.util.DeviceConfiguration
import com.rfcoding.core.presentation.util.currentDeviceConfiguration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun ColumnScope.ChatParticipantSelectionSection(
    existingParticipants: List<ChatParticipantUi>,
    selectedParticipants: List<ChatParticipantUi>,
    creator: ChatParticipantUi?,
    onRemoveClick: (ChatParticipantUi) -> Unit,
    modifier: Modifier = Modifier
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
            if (existingParticipants.isNotEmpty()) {
                items(
                    items = existingParticipants,
                    key = { "existing_${it.id}" }
                ) { participant ->
                    ChatParticipantListItem(
                        participant = participant,
                        showRemoveButton = creator?.id != participant.id,
                        modifier = Modifier.fillMaxWidth(),
                        onRemoveClick = {
                            onRemoveClick(participant)
                        }
                    )
                }
                item {
                    ChirpHorizontalDivider()
                }
            }

            if (selectedParticipants.isNotEmpty()) {
                items(
                    items = selectedParticipants,
                    key = { it.id }
                ) { participant ->
                    ChatParticipantListItem(
                        participant = participant,
                        showRemoveButton = true,
                        modifier = Modifier.fillMaxWidth(),
                        onRemoveClick = {
                            onRemoveClick(participant)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatParticipantListItem(
    participant: ChatParticipantUi,
    showRemoveButton: Boolean,
    modifier: Modifier = Modifier,
    onRemoveClick: () -> Unit
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
        if (showRemoveButton) {
            ChirpIconButton(
                onClick = onRemoveClick
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
@Preview
private fun ChatParticipantSelectionSectionPreview() {
    ChirpTheme {
        val selectedParticipants = mutableListOf<ChatParticipantUi>()
        repeat(3) {
            selectedParticipants.add(
                ChatParticipantUi(
                    id = Uuid.random().toString(),
                    username = "chinley$it",
                    initial = "CC"
                )
            )
        }
        val existingParticipants = selectedParticipants.take(2).map {
            it.copy(id = Uuid.random().toString())
        }

        Column {
            ChatParticipantSelectionSection(
                existingParticipants = existingParticipants,
                selectedParticipants = selectedParticipants,
                creator = null,
                onRemoveClick = {}
            )
        }
    }
}