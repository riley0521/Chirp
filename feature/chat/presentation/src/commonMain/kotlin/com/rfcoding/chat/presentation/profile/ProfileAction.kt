package com.rfcoding.chat.presentation.profile

sealed interface ProfileAction {
    data object OnDismiss: ProfileAction
    data object OnUploadPictureClick: ProfileAction
    data object OnErrorImagePicker: ProfileAction
    class OnPictureSelected(val bytes: ByteArray, val mimeType: String?): ProfileAction
    data object OnDeletePictureClick: ProfileAction
    data object OnConfirmDeleteClick: ProfileAction
    data object OnCancelDeleteClick: ProfileAction
    data object OnToggleCurrentPasswordVisibility: ProfileAction
    data object OnToggleNewPasswordVisibility: ProfileAction
    data object OnChangePasswordClick: ProfileAction
}