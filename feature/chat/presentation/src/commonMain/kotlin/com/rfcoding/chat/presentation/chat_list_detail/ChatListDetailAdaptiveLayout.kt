package com.rfcoding.chat.presentation.chat_list_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun ChatListDetailAdaptiveLayout(
    viewModel: ChatListDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val scaffoldDirective = createNoSpacingPaneScaffoldDirective()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator(
        scaffoldDirective = scaffoldDirective
    )
    val scope = rememberCoroutineScope()

    BackHandler(enabled = scaffoldNavigator.canNavigateBack()) {
        scope.launch {
            scaffoldNavigator.navigateBack()
        }
    }

    ListDetailPaneScaffold(
        directive = scaffoldDirective,
        value = scaffoldNavigator.scaffoldValue,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.extended.surfaceLower),
        listPane = {
            AnimatedPane {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(100) { index ->
                        Text(
                            text = "Chat $index",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onAction(ChatListDetailAction.OnChatClick(index.toString()))
                                    scope.launch {
                                        scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                                    }
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }
        },
        detailPane = {
            AnimatedPane {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    state.selectedChatId?.let {
                        Text(text = it)
                    }
                }
            }
        }
    )
}

@Composable
@Preview
private fun ChatListDetailAdaptiveLayoutPreview() {
    ChirpTheme {
        ChatListDetailAdaptiveLayout()
    }
}