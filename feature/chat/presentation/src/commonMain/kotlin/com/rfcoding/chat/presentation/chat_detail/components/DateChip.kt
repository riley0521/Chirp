package com.rfcoding.chat.presentation.chat_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DateChip(
    date: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.extended.surfaceOutline,
                shape = RoundedCornerShape(100)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date,
            modifier = Modifier
                .padding(
                    vertical = 4.dp,
                    horizontal = 12.dp
                ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.extended.textPlaceholder
        )
    }
}

@Composable
@Preview
private fun DateChipPreview() {
    ChirpTheme {
        DateChip(
            date = "November 5"
        )
    }
}