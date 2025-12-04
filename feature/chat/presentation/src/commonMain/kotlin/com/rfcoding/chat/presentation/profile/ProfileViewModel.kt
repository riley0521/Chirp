package com.rfcoding.chat.presentation.profile 
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfcoding.core.domain.validation.PasswordValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(ProfileState())
    val state = _state
        .onStart {
            if(!hasLoadedInitialData) {
                observeFormInputs()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProfileState()
        )
        
        fun onAction(action: ProfileAction) {
            when(action) {
                else -> Unit
            }
        }

    private val currentPasswordTextFlow = snapshotFlow { state.value.currentPasswordTextState.text.toString() }
    private val newPasswordTextFlow = snapshotFlow { state.value.newPasswordTextState.text.toString() }

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

}