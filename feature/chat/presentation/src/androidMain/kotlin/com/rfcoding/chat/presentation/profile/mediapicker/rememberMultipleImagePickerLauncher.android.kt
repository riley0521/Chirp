package com.rfcoding.chat.presentation.profile.mediapicker

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

@Composable
actual fun rememberMultipleImagePickerLauncher(onResult: (List<PickedImageData>) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
        onResult = { images ->
            val pickedImagesDeferred = images.map { imageUri ->
                val parser = ContentUriParser(context)
                scope.async {
                    val bytes = parser.readUri(imageUri) ?: return@async null
                    val mimeType = parser.getMimeType(imageUri)

                    PickedImageData(
                        bytes = bytes,
                        mimeType = mimeType
                    )
                }
            }

            scope.launch {
                onResult(pickedImagesDeferred.awaitAll().filterNotNull())
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