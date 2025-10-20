package com.rfcoding.core.designsystem.components.textfields

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PlatformImeOptions
import platform.UIKit.UIKeyboardTypeDefault
import platform.UIKit.UIKeyboardTypeEmailAddress
import platform.UIKit.UIReturnKeyType

@OptIn(ExperimentalComposeUiApi::class)
actual fun getPlatformImeOptions(keyboardType: KeyboardType, imeAction: ImeAction): PlatformImeOptions? {
    val kbType = when (keyboardType) {
        KeyboardType.Email -> UIKeyboardTypeEmailAddress
        else -> UIKeyboardTypeDefault
    }
    val ime = when (imeAction) {
        ImeAction.Next -> UIReturnKeyType.UIReturnKeyNext
        ImeAction.Go -> UIReturnKeyType.UIReturnKeyGo
        else -> UIReturnKeyType.UIReturnKeyDefault
    }

    return PlatformImeOptions {
        keyboardType(kbType)
        returnKeyType(ime)
    }
}