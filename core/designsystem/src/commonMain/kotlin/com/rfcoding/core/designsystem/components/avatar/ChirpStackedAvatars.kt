package com.rfcoding.core.designsystem.components.avatar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.rfcoding.core.designsystem.theme.ChirpTheme

@Composable
fun ChirpStackedAvatars(
    avatars: List<ChatParticipantUi?>,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.SMALL,
    maxVisible: Int = 2,
    overlapPercentage: Float = 0.4f
) {
    val overlapOffset = -(size.dp * overlapPercentage)

    val visibleAvatars = avatars.take(maxVisible)
    val remainingCount = (avatars.size - maxVisible).coerceIn(0, 99)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(overlapOffset),
        verticalAlignment = Alignment.CenterVertically
    ) {
        visibleAvatars.forEach { avatarUi ->
            ChirpAvatarPhoto(
                displayText = avatarUi?.initial ?: "??",
                size = size,
                imageUrl = avatarUi?.imageUrl
            )
        }

        if (remainingCount > 0) {
            ChirpAvatarPhoto(
                displayText = "$remainingCount+",
                size = size,
                textColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
@Preview
private fun ChirpStackedAvatarsPreview() {
    ChirpTheme {
        ChirpStackedAvatars(
            avatars = listOf(
                ChatParticipantUi(
                    id = "1",
                    username = "chinley",
                    initial = "CL"
                ),
                ChatParticipantUi(
                    id = "2",
                    username = "riley",
                    initial = "RF"
                ),
                ChatParticipantUi(
                    id = "3",
                    username = "chin",
                    initial = "AB"
                ),
                ChatParticipantUi(
                    id = "4",
                    username = "chin",
                    initial = "AB"
                )
            )
        )
    }
}