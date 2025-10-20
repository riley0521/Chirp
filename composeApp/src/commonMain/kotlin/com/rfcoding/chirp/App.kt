package com.rfcoding.chirp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.rfcoding.chirp.navigation.DeepLinkListener
import com.rfcoding.chirp.navigation.NavigationRoot
import com.rfcoding.core.designsystem.theme.ChirpTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val navController = rememberNavController()
    DeepLinkListener(navController)
    ChirpTheme {
        NavigationRoot(navController)
    }
}