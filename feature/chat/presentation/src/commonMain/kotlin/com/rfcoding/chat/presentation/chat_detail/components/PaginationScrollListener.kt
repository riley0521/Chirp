package com.rfcoding.chat.presentation.chat_detail.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun PaginationScrollListener(
    lazyListState: LazyListState,
    itemCount: Int,
    isPaginationLoading: Boolean,
    isEndReached: Boolean,
    onNearTop: () -> Unit,
    onNearBottom: (Boolean) -> Unit
) {
    val updatedItemCount by rememberUpdatedState(itemCount)
    val updatedIsPaginationLoading by rememberUpdatedState(isPaginationLoading)
    val updatedIsEndReached by rememberUpdatedState(isEndReached)
    var lastTriggerItemCount by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            val info = lazyListState.layoutInfo
            val total = info.totalItemsCount
            val topVisibleIndex = info.visibleItemsInfo.lastOrNull()?.index
            val remainingItems = if (topVisibleIndex != null) {
                total - topVisibleIndex - 1
            } else null
            val isNearBottom = info.visibleItemsInfo.any {
                it.index == 0
            }

            PaginationScrollState(
                currentItemCount = updatedItemCount,
                isEligible = remainingItems != null &&
                        remainingItems <= 5 &&
                        !updatedIsPaginationLoading &&
                        !updatedIsEndReached,
                isNearBottom = isNearBottom
            )
        }.distinctUntilChanged()
            .collect { (currentItemCount, isEligible, isNearBottom) ->
                val shouldTrigger = isEligible && currentItemCount > lastTriggerItemCount

                if (shouldTrigger) {
                    lastTriggerItemCount = currentItemCount
                    onNearTop()
                }

                onNearBottom(isNearBottom)
            }
    }
}

data class PaginationScrollState(
    val currentItemCount: Int,
    val isEligible: Boolean,
    val isNearBottom: Boolean
)