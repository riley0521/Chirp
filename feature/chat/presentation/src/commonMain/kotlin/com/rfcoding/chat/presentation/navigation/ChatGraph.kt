package com.rfcoding.chat.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.rfcoding.chat.presentation.chat_list_detail.ChatListDetailAdaptiveLayout

fun NavGraphBuilder.chatGraph(
    onConfirmLogout: () -> Unit
) {
    navigation<ChatGraphRoutes.Graph>(
        startDestination = ChatGraphRoutes.ChatListDetail(null)
    ) {
        composable<ChatGraphRoutes.ChatListDetail>(
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "chirp://chat_detail/{chatId}"
                }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.toRoute<ChatGraphRoutes.ChatListDetail>().chatId
            ChatListDetailAdaptiveLayout(
                initialChatId = chatId,
                onConfirmLogout = {
                    // TODO: Remove user session before calling onConfirmLogout to really navigate
                    // back to Login screen.
                }
            )
        }
    }
}