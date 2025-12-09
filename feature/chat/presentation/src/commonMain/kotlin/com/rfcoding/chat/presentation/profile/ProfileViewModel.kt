package com.rfcoding.chat.presentation.profile

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.core.domain.auth.AuthService
import com.rfcoding.core.domain.util.Result
import com.rfcoding.core.domain.validation.PasswordValidator
import com.rfcoding.core.presentation.util.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authService: AuthService
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(ProfileState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeFormInputs()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProfileState()
        )

    private val currentPasswordTextFlow =
        snapshotFlow { state.value.currentPasswordTextState.text.toString() }
    private val newPasswordTextFlow =
        snapshotFlow { state.value.newPasswordTextState.text.toString() }

    private fun observeFormInputs() {
        combine(
            currentPasswordTextFlow,
            newPasswordTextFlow
        ) { currentPassword, newPassword ->
            val newPasswordIsValid = PasswordValidator.validate(newPassword).isValidPassword
            val canChangePassword = currentPassword.isNotBlank() && newPasswordIsValid

            _state.update {
                it.copy(
                    newPasswordCriteriaNotMet = !newPasswordIsValid,
                    canChangePassword = canChangePassword
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnChangePasswordClick -> changePassword()
            ProfileAction.OnCancelDeleteClick -> {
                _state.update { it.copy(showDeleteConfirmationDialog = false) }
            }
            ProfileAction.OnConfirmDeleteClick -> deleteProfilePhoto()
            ProfileAction.OnDeletePictureClick -> {
                _state.update { it.copy(showDeleteConfirmationDialog = true) }
            }
            ProfileAction.OnErrorImagePicker -> Unit
            ProfileAction.OnOpenImagePicker -> Unit
            is ProfileAction.OnPictureSelected -> Unit
            ProfileAction.OnToggleCurrentPasswordVisibility -> {
                _state.update { it.copy(isCurrentPasswordVisible = !it.isCurrentPasswordVisible) }
            }
            ProfileAction.OnToggleNewPasswordVisibility -> {
                _state.update { it.copy(isNewPasswordVisible = !it.isNewPasswordVisible) }
            }
            ProfileAction.OnUploadPictureClick -> Unit
            is ProfileAction.OnUriSelected -> Unit
            ProfileAction.OnDismiss -> Unit
        }
    }

    private fun changePassword() {
        if (!state.value.canChangePassword || state.value.isChangingPassword) {
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isChangingPassword = true,
                    currentPasswordError = null,
                    isPasswordChangeSuccessful = false
                )
            }

            val oldPassword = state.value.currentPasswordTextState.text.toString()
            val newPassword = state.value.newPasswordTextState.text.toString()
            when (val result = authService.changePassword(oldPassword, newPassword)) {
                is Result.Failure -> {
                    _state.update { it.copy(currentPasswordError = result.toUiText()) }
                }
                is Result.Success -> {
                    state.value.currentPasswordTextState.clearText()
                    state.value.newPasswordTextState.clearText()

                    _state.update {
                        it.copy(
                            currentPasswordError = null,
                            isCurrentPasswordVisible = false,
                            isNewPasswordVisible = false,
                            isPasswordChangeSuccessful = true
                        )
                    }
                }
            }

            _state.update { it.copy(isChangingPassword = false) }
        }
    }

    private fun deleteProfilePhoto() {
        _state.update { it.copy(showDeleteConfirmationDialog = false) }
        // TODO
    }
}