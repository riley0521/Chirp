package com.rfcoding.core.domain.util

import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

class Paginator<Key, Item>(
    private val initialKey: Key,
    private val onLoadUpdated: (Boolean) -> Unit,
    private val onRequest: suspend (nextKey: Key) -> Result<List<Item>, DataError>,
    private val getNextKey: suspend (List<Item>) -> Key,
    private val onError: suspend (DataError?) -> Unit,
    private val onSuccess: suspend (items: List<Item>, nextKey: Key) -> Unit
) {

    private var currentKey = initialKey
    private var isMakingRequest = false
    private var lastRequestKey: Key? = null

    suspend fun loadNextItems() {
        if (isMakingRequest) {
            return
        }

        if (currentKey != null && currentKey == lastRequestKey) {
            return
        }

        isMakingRequest = true
        onLoadUpdated(true)

        try {
            val result = onRequest(currentKey)
            when (result) {
                is Result.Failure -> {
                    onError(result.error)
                }
                is Result.Success -> {
                    val items = result.data
                    val nextKey = getNextKey(items)
                    onSuccess(items, nextKey)

                    lastRequestKey = currentKey
                    currentKey = nextKey
                }
            }
        } catch (_: Exception) {
            coroutineContext.ensureActive()
            onError(DataError.Remote.UNKNOWN)
        } finally {
            onLoadUpdated(false)
            isMakingRequest = false
        }
    }

    fun reset() {
        currentKey = initialKey
        lastRequestKey = null
    }
}