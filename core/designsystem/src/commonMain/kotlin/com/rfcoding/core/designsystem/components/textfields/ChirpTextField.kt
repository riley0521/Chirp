package com.rfcoding.core.designsystem.components.textfields

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended

@Composable
fun ChirpTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    title: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = false,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Unspecified,
    onFocusChanged: (Boolean) -> Unit = {},
    onKeyboardAction: KeyboardActionHandler? = null
) {
    ChirpTextFieldLayout(
        modifier = modifier,
        title = title,
        supportingText = supportingText,
        isError = isError,
        enabled = enabled,
        onFocusChanged = onFocusChanged
    ) { textFieldStyleModifier, interactionSource ->
        BasicTextField(
            state = state,
            enabled = enabled,
            lineLimits = if (singleLine) {
                TextFieldLineLimits.SingleLine
            } else TextFieldLineLimits.Default,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else MaterialTheme.colorScheme.extended.textPlaceholder
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
                platformImeOptions = getPlatformImeOptions(keyboardType, imeAction)
            ),
            onKeyboardAction = onKeyboardAction,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            interactionSource = interactionSource,
            modifier = textFieldStyleModifier,
            decorator = { innerBox ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (state.text.isEmpty() && placeholder != null) {
                        Text(
                            text = placeholder,
                            color = MaterialTheme.colorScheme.extended.textPlaceholder,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    innerBox()
                }
            }
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun ChirpTextFieldEmptyPreview() {
    ChirpTheme {
        val state = rememberTextFieldState()

        ChirpTextField(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            placeholder = "test@test.com",
            title = "Email",
            supportingText = "Please enter your email"
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun ChirpTextFieldFilledPreview() {
    ChirpTheme {
        val state = rememberTextFieldState(initialText = "sample@chirp.com")

        ChirpTextField(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            placeholder = "test@test.com",
            title = "Email",
            supportingText = "Please enter your email"
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun ChirpTextFieldDisabledPreview() {
    ChirpTheme {
        val state = rememberTextFieldState()

        ChirpTextField(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            placeholder = "test@test.com",
            title = "Email",
            supportingText = "Please enter your email",
            enabled = false
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun ChirpTextFieldErrorPreview() {
    ChirpTheme {
        val state = rememberTextFieldState()

        ChirpTextField(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            placeholder = "test@test.com",
            title = "Email",
            supportingText = "This is not a valid email",
            isError = true
        )
    }
}