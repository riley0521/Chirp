package com.rfcoding.chat.presentation.profile.mediapicker

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UniformTypeIdentifiers.UTType
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_group_create
import platform.darwin.dispatch_group_enter
import platform.darwin.dispatch_group_leave
import platform.darwin.dispatch_group_notify
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
class IOSPickerViewControllerDelegate(
    private val scope: CoroutineScope,
    private val onResult: (List<PickedImageData>) -> Unit
): NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(
        picker: PHPickerViewController,
        didFinishPicking: List<*>
    ) {
        picker.dismissViewControllerAnimated(true, null)
        val results = didFinishPicking.filterIsInstance<PHPickerResult>()

        val dispatchGroup = dispatch_group_create()
        val imageDataList = mutableListOf<PickedImageData>()

        for (result in results) {
            dispatch_group_enter(dispatchGroup)
            val itemProvider = result.itemProvider
            val typeIdentifiers = itemProvider.registeredTypeIdentifiers
            val primaryType = typeIdentifiers.firstOrNull() as? String

            if (primaryType == null) {
                dispatch_group_leave(dispatchGroup)
                continue
            }

            val mimeType = UTType.typeWithIdentifier(primaryType)?.preferredMIMEType

            if (mimeType == null) {
                dispatch_group_leave(dispatchGroup)
                continue
            }

            itemProvider.loadDataRepresentationForTypeIdentifier(
                typeIdentifier = primaryType
            ) { nsData, nsError ->
                scope.launch {
                    nsData?.let {
                        val bytes = ByteArray(nsData.length.toInt())

                        withContext(Dispatchers.Default) {
                            memcpy(bytes.refTo(0), nsData.bytes, nsData.length)
                        }

                        imageDataList.add(
                            PickedImageData(
                                bytes = bytes,
                                mimeType = mimeType
                            )
                        )
                    }
                    dispatch_group_leave(dispatchGroup)
                }
            }

            dispatch_group_notify(dispatchGroup, dispatch_get_main_queue()) {
                scope.launch {
                    onResult(imageDataList)
                }
            }
        }
    }
}