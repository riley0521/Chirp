package com.rfcoding.chat.presentation.profile.mediapicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerConfigurationSelectionOrdered
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerViewController
import platform.UIKit.UIApplication

@Composable
actual fun rememberMultipleImagePickerLauncher(onResult: (List<PickedImageData>) -> Unit): ImagePickerLauncher {
    val scope = rememberCoroutineScope()
    val delegate = remember {
        IOSPickerViewControllerDelegate(
            scope = scope,
            onResult = { images ->
                onResult(images)
            }
        )
    }

    return remember {
        val pickerViewController = PHPickerViewController(
            configuration = PHPickerConfiguration().apply {
                setSelectionLimit(1)
                setFilter(PHPickerFilter.imagesFilter)
                setSelection(PHPickerConfigurationSelectionOrdered)
            }
        )
        pickerViewController.delegate = delegate

        ImagePickerLauncher(
            onLaunch = {
                UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                    pickerViewController,
                    true,
                    null
                )
            }
        )
    }
}