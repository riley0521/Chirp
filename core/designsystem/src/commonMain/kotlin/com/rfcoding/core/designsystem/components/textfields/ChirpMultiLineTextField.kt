package com.rfcoding.core.designsystem.components.textfields

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rfcoding.core.designsystem.components.buttons.ChirpButton
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ImageData(
    val id: String,
    val bytes: ByteArray
)

@Composable
fun ChirpMultiLineTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    images: List<ImageData> = emptyList(),
    onRemoveImage: (String) -> Unit = {},
    enabled: Boolean = true,
    showHeader: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    maxHeightInLines: Int = 3,
    altHeaderContent: @Composable (ColumnScope.() -> Unit)? = null,
    bottomContent: @Composable (RowScope.() -> Unit)? = null
) {
    val focusRequester = remember {
        FocusRequester()
    }

    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.extended.surfaceLower,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.extended.surfaceOutline,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = null,
                indication = null
            ) {
                focusRequester.requestFocus()
            }
            .padding(
                vertical = 12.dp,
                horizontal = 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showHeader) {
            BasicTextField(
                state = state,
                enabled = enabled,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.extended.textPrimary
                ),
                lineLimits = TextFieldLineLimits.MultiLine(
                    minHeightInLines = 1,
                    maxHeightInLines = maxHeightInLines
                ),
                keyboardOptions = keyboardOptions,
                onKeyboardAction = onKeyboardAction,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.extended.textPrimary),
                decorator = { innerBox ->
                    if (placeholder != null && state.text.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = MaterialTheme.colorScheme.extended.textPlaceholder,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    innerBox()
                },
                modifier = Modifier.focusRequester(focusRequester)
            )
            if (images.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(images) { image ->
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            AsyncImage(
                                model = image.bytes,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .matchParentSize()
                            )

                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable(
                                        enabled = true,
                                        onClick = {
                                            onRemoveImage(image.id)
                                        }
                                    )
                                    .background(MaterialTheme.colorScheme.onBackground),
                                contentAlignment = Alignment.TopEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        } else {
            altHeaderContent?.invoke(this)
        }
        bottomContent?.let {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                it()
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
@Preview
private fun ChirpMultiLineTextFieldPreview() {
    ChirpTheme {
        ChirpMultiLineTextField(
            state = rememberTextFieldState(initialText = ""),
            placeholder = "Send a message",
            images = listOf(
                ImageData(
                    id = Uuid.random().toString(),
                    bytes = "sample".encodeToByteArray()
                )
            ),
            modifier = Modifier
                .fillMaxWidth(),
            bottomContent = {
                Spacer(modifier = Modifier.weight(1f))
                ChirpButton(
                    text = "Send",
                    onClick = {}
                )
            }
        )
    }
}