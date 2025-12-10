package com.rfcoding.chat.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.core.designsystem.generated.resources.upload_icon
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.cancel
import chirp.feature.chat.presentation.generated.resources.contact_to_change_email
import chirp.feature.chat.presentation.generated.resources.current_password
import chirp.feature.chat.presentation.generated.resources.delete
import chirp.feature.chat.presentation.generated.resources.delete_profile_image
import chirp.feature.chat.presentation.generated.resources.delete_profile_image_desc
import chirp.feature.chat.presentation.generated.resources.email
import chirp.feature.chat.presentation.generated.resources.new_password
import chirp.feature.chat.presentation.generated.resources.password
import chirp.feature.chat.presentation.generated.resources.password_change_successful
import chirp.feature.chat.presentation.generated.resources.password_hint
import chirp.feature.chat.presentation.generated.resources.profile_image
import chirp.feature.chat.presentation.generated.resources.save
import chirp.feature.chat.presentation.generated.resources.upload_image
import com.rfcoding.chat.presentation.profile.components.ProfileHeaderSection
import com.rfcoding.chat.presentation.profile.components.ProfileSectionLayout
import com.rfcoding.chat.presentation.profile.mediapicker.rememberImagePickerLauncher
import com.rfcoding.core.designsystem.components.avatar.AvatarSize
import com.rfcoding.core.designsystem.components.avatar.ChirpAvatarPhoto
import com.rfcoding.core.designsystem.components.brand.ChirpHorizontalDivider
import com.rfcoding.core.designsystem.components.buttons.ChirpButton
import com.rfcoding.core.designsystem.components.buttons.ChirpButtonStyle
import com.rfcoding.core.designsystem.components.dialogs.ChirpAdaptiveDialogSheetLayout
import com.rfcoding.core.designsystem.components.dialogs.DestructiveConfirmationDialog
import com.rfcoding.core.designsystem.components.textfields.ChirpPasswordTextField
import com.rfcoding.core.designsystem.components.textfields.ChirpTextField
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.presentation.util.UiText
import com.rfcoding.core.presentation.util.clearFocusOnTap
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import chirp.core.designsystem.generated.resources.Res as DesignSystemRes

@Composable
fun ProfileRoot(
    onDismiss: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val launcher = rememberImagePickerLauncher(
        onResult = { data ->
            viewModel.onAction(
                ProfileAction.OnPictureSelected(
                    bytes = data.bytes,
                    mimeType = data.mimeType
                )
            )
        }
    )

    ChirpAdaptiveDialogSheetLayout(
        onDismiss = onDismiss
    ) {
        ProfileScreen(
            state = state,
            onAction = { action ->
                when (action) {
                    ProfileAction.OnDismiss -> onDismiss()
                    ProfileAction.OnUploadPictureClick -> {
                        launcher.launch()
                    }
                    else -> Unit
                }

                viewModel.onAction(action)
            }
        )
    }
}

@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .clearFocusOnTap()
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeaderSection(
            username = state.username,
            onCloseClick = {
                onAction(ProfileAction.OnDismiss)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp)
        )
        ChirpHorizontalDivider()
        ProfileSectionLayout(
            headerText = stringResource(Res.string.profile_image)
        ) {
            Row {
                ChirpAvatarPhoto(
                    displayText = state.userInitials,
                    size = AvatarSize.LARGE,
                    imageUrl = state.profilePictureUrl,
                    onClick = if (!state.isUploadingImage) {
                        {
                            onAction(ProfileAction.OnUploadPictureClick)
                        }
                    } else null
                )
                Spacer(modifier = Modifier.width(20.dp))
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ChirpButton(
                        text = stringResource(Res.string.upload_image),
                        onClick = {
                            onAction(ProfileAction.OnUploadPictureClick)
                        },
                        style = ChirpButtonStyle.SECONDARY,
                        enabled = state.isActionEnabled,
                        isLoading = state.isUploadingImage,
                        leadingIcon = {
                            Icon(
                                imageVector = vectorResource(DesignSystemRes.drawable.upload_icon),
                                contentDescription = stringResource(Res.string.upload_image)
                            )
                        }
                    )
                    ChirpButton(
                        text = stringResource(Res.string.delete),
                        onClick = {
                            onAction(ProfileAction.OnDeletePictureClick)
                        },
                        style = ChirpButtonStyle.DESTRUCTIVE_SECONDARY,
                        enabled = state.isActionEnabled && state.profilePictureUrl != null,
                        isLoading = state.isDeletingImage,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.delete_profile_image)
                            )
                        }
                    )
                }
            }
            state.imageError?.let {
                Text(
                    text = it.asString(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        ChirpHorizontalDivider()
        ProfileSectionLayout(
            headerText = stringResource(Res.string.email)
        ) {
            ChirpTextField(
                state = state.emailTextState,
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                supportingText = stringResource(Res.string.contact_to_change_email)
            )
        }
        ChirpHorizontalDivider()
        ProfileSectionLayout(
            headerText = stringResource(Res.string.password)
        ) {
            ChirpPasswordTextField(
                state = state.currentPasswordTextState,
                isPasswordVisible = state.isCurrentPasswordVisible,
                onToggleVisibilityClick = {
                    onAction(ProfileAction.OnToggleCurrentPasswordVisibility)
                },
                enabled = state.isActionEnabled,
                placeholder = stringResource(Res.string.current_password),
                isError = state.currentPasswordError != null,
                supportingText = state.currentPasswordError?.asString()
            )
            ChirpPasswordTextField(
                state = state.newPasswordTextState,
                isPasswordVisible = state.isNewPasswordVisible,
                onToggleVisibilityClick = {
                    onAction(ProfileAction.OnToggleNewPasswordVisibility)
                },
                enabled = state.isActionEnabled,
                placeholder = stringResource(Res.string.new_password),
                isError = state.newPasswordCriteriaNotMet,
                supportingText = stringResource(Res.string.password_hint)
            )
            if (state.isPasswordChangeSuccessful) {
                Text(
                    text = stringResource(Res.string.password_change_successful),
                    color = MaterialTheme.colorScheme.extended.success,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
            ) {
                ChirpButton(
                    text = stringResource(Res.string.save),
                    onClick = {
                        onAction(ProfileAction.OnChangePasswordClick)
                    },
                    enabled = state.canChangePassword && !state.isChangingPassword,
                    isLoading = state.isChangingPassword
                )
            }
        }
    }

    if (state.showDeleteConfirmationDialog) {
        DestructiveConfirmationDialog(
            title = stringResource(Res.string.delete_profile_image),
            description = stringResource(Res.string.delete_profile_image_desc),
            confirmButtonText = stringResource(Res.string.delete),
            cancelButtonText = stringResource(Res.string.cancel),
            onConfirmClick = {
                onAction(ProfileAction.OnConfirmDeleteClick)
            },
            onCancelClick = {
                onAction(ProfileAction.OnCancelDeleteClick)
            },
            onDismiss = {
                onAction(ProfileAction.OnCancelDeleteClick)
            }
        )
    }
}

@Preview
@Composable
private fun Preview() {
    ChirpTheme {
        ProfileScreen(
            state = ProfileState(
                username = "Maria",
                userInitials = "MA",
                isPasswordChangeSuccessful = true,
                imageError = UiText.DynamicText("Something went wrong.")
            ),
            onAction = {}
        )
    }
}