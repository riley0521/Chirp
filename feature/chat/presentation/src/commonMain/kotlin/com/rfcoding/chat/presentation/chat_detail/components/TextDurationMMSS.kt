package com.rfcoding.chat.presentation.chat_detail.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.presentation.util.formatMMSS
import kotlin.time.Duration

@Composable
fun TextDurationMMSS(
    duration: Duration,
    modifier: Modifier = Modifier
) {
    Text(
        text = duration.formatMMSS(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontFeatureSettings = "tnum"
        ),
        color = MaterialTheme.colorScheme.extended.textSecondary,
        modifier = modifier
    )
}