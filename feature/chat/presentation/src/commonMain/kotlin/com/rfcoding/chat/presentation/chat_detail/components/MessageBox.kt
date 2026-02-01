package com.rfcoding.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chirp.core.designsystem.generated.resources.check_icon
import chirp.core.designsystem.generated.resources.clip_icon
import chirp.core.designsystem.generated.resources.cloud_off_icon
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.attach_image
import chirp.feature.chat.presentation.generated.resources.cancel
import chirp.feature.chat.presentation.generated.resources.finish_voice_messaging
import chirp.feature.chat.presentation.generated.resources.send
import chirp.feature.chat.presentation.generated.resources.send_message
import com.rfcoding.chat.domain.models.ConnectionState
import com.rfcoding.chat.presentation.model.TrackSizeInfo
import com.rfcoding.chat.presentation.util.toUiText
import com.rfcoding.core.designsystem.components.buttons.ChirpButton
import com.rfcoding.core.designsystem.components.buttons.ChirpButtonStyle
import com.rfcoding.core.designsystem.components.buttons.ChirpIconButton
import com.rfcoding.core.designsystem.components.textfields.ChirpMultiLineTextField
import com.rfcoding.core.designsystem.components.textfields.ImageData
import com.rfcoding.core.designsystem.components.textfields.getPlatformImeOptions
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.presentation.util.DeviceConfiguration
import com.rfcoding.core.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import chirp.core.designsystem.generated.resources.Res as DesignSystemRes

@Composable
fun MessageBox(
    messageTextFieldState: TextFieldState,
    isTextInputEnabled: Boolean,
    connectionState: ConnectionState,
    images: List<ImageData>,
    isOnVoiceMessage: Boolean,
    recordingElapsedDuration: Duration,
    amplitudes: List<Float>,
    onSendClick: () -> Unit,
    onAttachImageClick: () -> Unit,
    onRemoveImage: (String) -> Unit,
    onVoiceMessageClick: () -> Unit,
    onConfirmVoiceMessageClick: () -> Unit,
    onCancelVoiceMessageClick: () -> Unit,
    modifier: Modifier = Modifier,
    amplitudeBarWidth: Dp = 5.dp,
    amplitudeBarSpacing: Dp = 4.dp
) {
    val isConnected = connectionState == ConnectionState.CONNECTED
    val isMobilePortrait = currentDeviceConfiguration() == DeviceConfiguration.MOBILE_PORTRAIT
    val canVoiceMessage = isTextInputEnabled &&
            messageTextFieldState.text.isBlank() &&
            isConnected

    val density = LocalDensity.current
    var trackSizeInfo by remember {
        mutableStateOf(
            TrackSizeInfo(trackWidth = 0.0f, barWidth = 0.0f, spacing = 0.0f)
        )
    }
    val barsCount by remember {
        derivedStateOf {
            if (!trackSizeInfo.isValid) {
                return@derivedStateOf 0
            }

            (trackSizeInfo.trackWidth / (trackSizeInfo.barWidth + trackSizeInfo.spacing)).roundToInt()
        }
    }

    ChirpMultiLineTextField(
        state = messageTextFieldState,
        modifier = modifier,
        placeholder = stringResource(Res.string.send_message),
        images = images,
        onRemoveImage = onRemoveImage,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Send,
            platformImeOptions = getPlatformImeOptions(KeyboardType.Text, ImeAction.Send),
            keyboardType = KeyboardType.Text
        ),
        onKeyboardAction = { onSendClick() },
        showHeader = !isOnVoiceMessage,
        altHeaderContent = {
            if (isMobilePortrait) {
                ChatPlayBar(
                    amplitudeBarWidth = amplitudeBarWidth,
                    amplitudeBarSpacing = amplitudeBarSpacing,
                    powerRatios = amplitudes.takeLast(barsCount),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .onSizeChanged { size ->
                            if (size.width > 0) {
                                trackSizeInfo = TrackSizeInfo(
                                    trackWidth = size.width.toFloat(),
                                    barWidth = with(density) { amplitudeBarWidth.toPx() },
                                    spacing = with(density) { amplitudeBarSpacing.toPx() }
                                )
                            }
                        }
                )
            }
        },
        bottomContent = {
            if (isOnVoiceMessage) {
                TextDurationMMSS(
                    duration = recordingElapsedDuration
                )
            }
            if (!isMobilePortrait) {
                ChatPlayBar(
                    amplitudeBarWidth = amplitudeBarWidth,
                    amplitudeBarSpacing = amplitudeBarSpacing,
                    powerRatios = amplitudes.takeLast(barsCount),
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .onSizeChanged { size ->
                            if (size.width > 0) {
                                trackSizeInfo = TrackSizeInfo(
                                    trackWidth = size.width.toFloat(),
                                    barWidth = with(density) { amplitudeBarWidth.toPx() },
                                    spacing = with(density) { amplitudeBarSpacing.toPx() }
                                )
                            }
                        }
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
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
            if (isOnVoiceMessage) {
                ChirpButton(
                    text = stringResource(Res.string.cancel),
                    onClick = onCancelVoiceMessageClick,
                    style = ChirpButtonStyle.SECONDARY
                )
                ChirpIconButton(
                    onClick = onConfirmVoiceMessageClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    enabled = recordingElapsedDuration.inWholeSeconds >= 1
                ) {
                    Icon(
                        imageVector = vectorResource(DesignSystemRes.drawable.check_icon),
                        contentDescription = stringResource(Res.string.finish_voice_messaging)
                    )
                }
            } else {
                ChirpIconButton(
                    onClick = onAttachImageClick
                ) {
                    Icon(
                        imageVector = vectorResource(DesignSystemRes.drawable.clip_icon),
                        contentDescription = stringResource(Res.string.attach_image)
                    )
                }
                if (canVoiceMessage) {
                    ChirpIconButton(
                        onClick = onVoiceMessageClick,
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = stringResource(Res.string.finish_voice_messaging)
                        )
                    }
                } else {
                    ChirpButton(
                        text = stringResource(Res.string.send),
                        onClick = onSendClick,
                        enabled = isConnected && isTextInputEnabled
                    )
                }
            }
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
            val amplitudes = remember {
                (1..250).map {
                    Random.nextFloat()
                }
            }

            MessageBox(
                messageTextFieldState = rememberTextFieldState(initialText = ""),
                isTextInputEnabled = true,
                connectionState = ConnectionState.CONNECTED,
                images = emptyList(),
                isOnVoiceMessage = true,
                recordingElapsedDuration = 100.seconds,
                amplitudes = amplitudes,
                onSendClick = {},
                onAttachImageClick = {},
                onRemoveImage = {},
                onVoiceMessageClick = {},
                onConfirmVoiceMessageClick = {},
                onCancelVoiceMessageClick = {}
            )
        }
    }
}