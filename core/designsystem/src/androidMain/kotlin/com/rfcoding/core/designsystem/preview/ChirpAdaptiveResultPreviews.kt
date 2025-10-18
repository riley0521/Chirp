package com.rfcoding.core.designsystem.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.rfcoding.core.designsystem.components.layouts.ChirpAdaptiveResultLayout
import com.rfcoding.core.designsystem.theme.ChirpTheme

@PreviewScreenSizes
@Composable
private fun ChirpAdaptiveResultPreviews() {
    ChirpTheme {
        ChirpAdaptiveResultLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Registration successful!",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@PreviewScreenSizes
@Composable
private fun ChirpAdaptiveResultDarkPreviews() {
    ChirpTheme(
        darkTheme = true
    ) {
        ChirpAdaptiveResultLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Registration successful!",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}