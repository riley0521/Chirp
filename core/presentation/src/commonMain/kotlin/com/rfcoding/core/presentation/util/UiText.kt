package com.rfcoding.core.presentation.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

sealed interface UiText {

    data class DynamicText(val value: String): UiText
    class Resource(
        val id: StringResource,
        val args: Array<Any> = arrayOf()
    ): UiText

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicText -> value
            is Resource -> stringResource(id, *args)
        }
    }

    suspend fun asStringAsync(): String {
        return when (this) {
            is DynamicText -> value
            is Resource -> getString(id, *args)
        }
    }
}