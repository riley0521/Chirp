package com.rfcoding.chirp

import androidx.compose.runtime.Composable
import com.rfcoding.chirp.navigation.NavigationRoot
import com.rfcoding.core.designsystem.theme.ChirpTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    ChirpTheme {
        NavigationRoot()
    }
}