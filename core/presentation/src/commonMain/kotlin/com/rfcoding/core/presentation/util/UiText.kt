package com.rfcoding.core.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

sealed interface UiText {

    data class DynamicText(val value: String): UiText
    class Resource(
        val id: StringResource,
        val args: Array<Any> = arrayOf()
    ): UiText

    data class StyledText(val value: AnnotatedString): UiText

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicText -> value
            is Resource -> stringResource(id, *args)
            is StyledText -> value.text
        }
    }

    @Composable
    fun asStyledText(): AnnotatedString {
        return when(this) {
            is StyledText -> value
            is DynamicText -> buildAnnotatedString { append(value) }
            is Resource -> buildAnnotatedString {
                append(stringResource(id, *args))
            }
        }
    }

    suspend fun asStringAsync(): String {
        return when (this) {
            is DynamicText -> value
            is Resource -> getString(id, *args)
            is StyledText -> value.text
        }
    }
}