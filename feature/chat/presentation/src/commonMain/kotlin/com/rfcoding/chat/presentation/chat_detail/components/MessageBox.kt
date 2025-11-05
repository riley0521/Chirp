package com.rfcoding.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import chirp.core.designsystem.generated.resources.cloud_off_icon
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.send
import chirp.feature.chat.presentation.generated.resources.send_message
import com.rfcoding.chat.domain.models.ConnectionState
import com.rfcoding.chat.presentation.util.toUiText
import com.rfcoding.core.designsystem.components.buttons.ChirpButton
import com.rfcoding.core.designsystem.components.textfields.ChirpMultiLineTextField
import com.rfcoding.core.designsystem.components.textfields.getPlatformImeOptions
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import chirp.core.designsystem.generated.resources.Res as DesignSystemRes

@Composable
fun MessageBox(
    messageTextFieldState: TextFieldState,
    isTextInputEnabled: Boolean,
    connectionState: ConnectionState,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = connectionState == ConnectionState.CONNECTED
    ChirpMultiLineTextField(
        state = messageTextFieldState,
        modifier = modifier
            .padding(4.dp),
        placeholder = stringResource(Res.string.send_message),
        enabled = isTextInputEnabled,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Send,
            platformImeOptions = getPlatformImeOptions(KeyboardType.Text, ImeAction.Send),
            keyboardType = KeyboardType.Text
        ),
        onKeyboardAction = { onSendClick() },
        bottomContent = {
            Spacer(modifier = Modifier.weight(1f))
            if (!isConnected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = vectorResource(DesignSystemRes.drawable.cloud_off_icon),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.extended.textDisabled
                    )
                    Text(
                        text = connectionState.toUiText().asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.extended.textDisabled
                    )
                }
            }
            ChirpButton(
                text = stringResource(Res.string.send),
                onClick = onSendClick,
                enabled = isConnected && isTextInputEnabled
            )
        }
    )
}

@Composable
@Preview
private fun MessageBoxPreview() {
    ChirpTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            MessageBox(
                messageTextFieldState = rememberTextFieldState(initialText = ""),
                isTextInputEnabled = true,
                connectionState = ConnectionState.CONNECTED,
                onSendClick = {}
            )
        }
    }
}