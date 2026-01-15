package com.rfcoding.chat.presentation.chat_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.core.designsystem.components.buttons.ChirpIconButton
import com.rfcoding.core.designsystem.theme.ChirpTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun ChatVoiceMessagePlayer(
    totalDuration: Duration,
    durationPlayed: Duration,
    hasStarted: Boolean,
    isPlaying: Boolean,
    isBuffering: Boolean,
    onTogglePlayback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationPlayedFloat = remember(durationPlayed) {
        durationPlayed.inWholeMilliseconds / totalDuration.inWholeMilliseconds.toFloat()
    }
    val durationDisplay = if (hasStarted) {
        durationPlayed
    } else {
        totalDuration
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChirpIconButton(
            onClick = onTogglePlayback,
            containerColor = if (!isPlaying) {
                MaterialTheme.colorScheme.primary
            } else MaterialTheme.colorScheme.surface
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .alpha(if (isPlaying && isBuffering) 1f else 0f)
                    .padding(8.dp)
            )

            Icon(
                imageVector = if (isPlaying) {
                    Icons.Default.Pause
                } else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier
                    .alpha(if (!isBuffering) 1f else 0f)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        LinearProgressIndicator(
            modifier = Modifier.weight(1f),
            progress = {
                durationPlayedFloat
            },
            drawStopIndicator = {}
        )
        Spacer(modifier = Modifier.width(12.dp))
        TextDurationMMSS(
            duration = durationDisplay
        )
    }
}

@Composable
@Preview
private fun ChatVoiceMessagePlayerPreview() {
    ChirpTheme {
        ChatVoiceMessagePlayer(
            totalDuration = 200.seconds,
            durationPlayed = 150.seconds,
            hasStarted = false,
            isPlaying = false,
            isBuffering = false,
            onTogglePlayback = {}
        )
    }
}