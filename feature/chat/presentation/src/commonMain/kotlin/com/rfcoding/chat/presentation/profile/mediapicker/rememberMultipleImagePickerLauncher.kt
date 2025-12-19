package com.rfcoding.chat.presentation.profile.mediapicker

import androidx.compose.runtime.Composable

@Composable
expect fun rememberMultipleImagePickerLauncher(
    onResult: (List<PickedImageData>) -> Unit
): ImagePickerLauncher