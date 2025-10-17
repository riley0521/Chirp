package com.rfcoding.core.designsystem.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.rfcoding.core.designsystem.components.brand.ChirpBrandLogo
import com.rfcoding.core.designsystem.components.layouts.ChirpAdaptiveFormLayout
import com.rfcoding.core.designsystem.theme.ChirpTheme

@Composable
@PreviewScreenSizes
private fun ChirpAdaptiveFormLayoutLightPreview() {
    ChirpTheme {
        ChirpAdaptiveFormLayout(
            headerText = "Welcome to Chirp!",
            errorText = "Login failed.",
            logo = {
                ChirpBrandLogo()
            },
            formContent = {
                Text(
                    text = "Nice galing!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        )
    }
}

@Composable
@PreviewScreenSizes
private fun ChirpAdaptiveFormLayoutDarkPreview() {
    ChirpTheme(
        darkTheme = true
    ) {
        ChirpAdaptiveFormLayout(
            headerText = "Welcome to Chirp!",
            errorText = "Login failed.",
            logo = {
                ChirpBrandLogo()
            },
            formContent = {
                Text(
                    text = "Nice galing!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        )
    }
}