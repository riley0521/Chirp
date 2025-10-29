package com.rfcoding.chat.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.rfcoding.chat.presentation.chat_list_detail.ChatListDetailAdaptiveLayout

fun NavGraphBuilder.chatGraph(
    navController: NavController
) {
    navigation<ChatGraphRoutes.Graph>(
        startDestination = ChatGraphRoutes.ChatListDetail
    ) {
        composable<ChatGraphRoutes.ChatListDetail> {
            ChatListDetailAdaptiveLayout()
        }
    }
}