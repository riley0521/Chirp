package com.rfcoding.core.designsystem.components.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rfcoding.core.presentation.util.currentDeviceConfiguration

@Composable
fun ChirpDialogContent(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    customDialog: Boolean = false,
    content: @Composable () -> Unit
) {
    val configuration = currentDeviceConfiguration()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = !customDialog)
    ) {
        val maxHeightForDevice = if (!configuration.isMobile) {
            540.dp
        } else {
            Dp.Unspecified
        }

        val maxHeight = if (customDialog) {
            Dp.Unspecified
        } else {
            maxHeightForDevice
        }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)
                .padding(horizontal = if (customDialog) 16.dp else 0.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            content()
        }
    }
}