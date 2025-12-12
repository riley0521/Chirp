package com.rfcoding.chirp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.rfcoding.auth.presentation.navigation.AuthGraphRoutes
import com.rfcoding.chat.presentation.navigation.ChatGraphRoutes
import com.rfcoding.chirp.navigation.DeepLinkListener
import com.rfcoding.chirp.navigation.NavigationRoot
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App(
    onAuthenticationChecked: () -> Unit = {},
    viewModel: MainViewModel = koinViewModel()
) {
    val navController = rememberNavController()

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isCheckingAuth) {
        if (!state.isCheckingAuth) {
            onAuthenticationChecked()
        }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            MainEvent.OnSessionExpired -> {
                navController.navigate(AuthGraphRoutes.Graph) {
                    popUpTo(AuthGraphRoutes.Graph) {
                        inclusive = false
                    }
                }
            }
        }
    }

    ChirpTheme {
        if (!state.isCheckingAuth) {
            NavigationRoot(
                navController = navController,
                startDestination = if (state.isLoggedIn) {
                    ChatGraphRoutes.Graph
                } else {
                    AuthGraphRoutes.Graph
                }
            )
            DeepLinkListener(navController)
        }
    }
}