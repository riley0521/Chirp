package com.rfcoding.chat.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.add
import chirp.feature.chat.presentation.generated.resources.email_or_username
import com.rfcoding.core.designsystem.components.buttons.ChirpButton
import com.rfcoding.core.designsystem.components.buttons.ChirpButtonStyle
import com.rfcoding.core.designsystem.components.textfields.ChirpTextField
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.presentation.util.UiText
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatParticipantSearchTextSection(
    queryState: TextFieldState,
    onAddClick: () -> Unit,
    isAddingEnabled: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    error: UiText? = null,
    onFocusChanged: (Boolean) -> Unit = {}
) {
    Row(
        modifier = modifier
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp
            )
            .height(intrinsicSize = IntrinsicSize.Max),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ChirpTextField(
            state = queryState,
            modifier = Modifier.weight(1f),
            placeholder = stringResource(Res.string.email_or_username),
            supportingText = error?.asString(),
            isError = error != null,
            onFocusChanged = onFocusChanged,
            singleLine = true,
            keyboardType = KeyboardType.Email
        )
        ChirpButton(
            text = stringResource(Res.string.add),
            onClick = onAddClick,
            style = ChirpButtonStyle.SECONDARY,
            enabled = isAddingEnabled,
            isLoading = isLoading
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun ChatMemberSearchTextSectionPreview() {
    ChirpTheme {
        ChatParticipantSearchTextSection(
            queryState = remember { TextFieldState(initialText = "") },
            onAddClick = {},
            isAddingEnabled = true,
            isLoading = false,
            error = UiText.DynamicText("Something went wrong."),
            onFocusChanged = {}
        )
    }
}