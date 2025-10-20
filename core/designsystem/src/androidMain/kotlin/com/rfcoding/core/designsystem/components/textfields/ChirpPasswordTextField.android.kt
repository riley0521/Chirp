package com.rfcoding.core.designsystem.components.textfields

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PlatformImeOptions

actual fun getPlatformImeOptions(
    keyboardType: KeyboardType,
    imeAction: ImeAction
): PlatformImeOptions? = null