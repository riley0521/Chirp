package com.rfcoding.chat.presentation.profile.mediapicker

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
actual fun rememberImagePickerLauncher(
    onResult: (PickedImageData) -> Unit
): ImagePickerLauncher {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { imageUri ->
            if (imageUri != null) {
                val parser = ContentUriParser(context)

                scope.launch {
                    val bytes = parser.readUri(imageUri) ?: return@launch
                    val mimeType = parser.getMimeType(imageUri)

                    onResult(
                        PickedImageData(
                            bytes = bytes,
                            mimeType = mimeType
                        )
                    )
                }
            }
        }
    )

    return remember {
        ImagePickerLauncher(
            onLaunch = {
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )
    }
}