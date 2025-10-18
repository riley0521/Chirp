package com.rfcoding.auth.presentation.register

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import chirp.feature.auth.presentation.generated.resources.Res
import chirp.feature.auth.presentation.generated.resources.email
import chirp.feature.auth.presentation.generated.resources.email_placeholder
import chirp.feature.auth.presentation.generated.resources.login
import chirp.feature.auth.presentation.generated.resources.password
import chirp.feature.auth.presentation.generated.resources.password_hint
import chirp.feature.auth.presentation.generated.resources.register
import chirp.feature.auth.presentation.generated.resources.username
import chirp.feature.auth.presentation.generated.resources.username_hint
import chirp.feature.auth.presentation.generated.resources.username_placeholder
import chirp.feature.auth.presentation.generated.resources.welcome_to_chirp
import com.rfcoding.core.designsystem.components.brand.ChirpBrandLogo
import com.rfcoding.core.designsystem.components.buttons.ChirpButton
import com.rfcoding.core.designsystem.components.buttons.ChirpButtonStyle
import com.rfcoding.core.designsystem.components.layouts.ChirpAdaptiveFormLayout
import com.rfcoding.core.designsystem.components.layouts.ChirpSnackbarScaffold
import com.rfcoding.core.designsystem.components.textfields.ChirpPasswordTextField
import com.rfcoding.core.designsystem.components.textfields.ChirpTextField
import com.rfcoding.core.designsystem.theme.ChirpTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RegisterRoot(
    viewModel: RegisterViewModel = viewModel() { RegisterViewModel() }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    RegisterScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun RegisterScreen(
    state: RegisterState,
    onAction: (RegisterAction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    ChirpSnackbarScaffold(
        snackbarHostState = snackbarHostState
    ) {
        ChirpAdaptiveFormLayout(
            headerText = stringResource(Res.string.welcome_to_chirp),
            logo = {
                ChirpBrandLogo()
            },
            errorText = state.registrationError?.asString()
        ) {
            ChirpTextField(
                state = state.usernameTextState,
                title = stringResource(Res.string.username),
                placeholder = stringResource(Res.string.username_placeholder),
                isError = state.usernameError != null,
                supportingText = state.usernameError?.asString() ?: stringResource(Res.string.username_hint)
            )
            Spacer(modifier = Modifier.height(20.dp))
            ChirpTextField(
                state = state.emailTextState,
                title = stringResource(Res.string.email),
                placeholder = stringResource(Res.string.email_placeholder),
                isError = state.emailError != null,
                supportingText = state.emailError?.asString()
            )
            Spacer(modifier = Modifier.height(20.dp))
            ChirpPasswordTextField(
                state = state.passwordTextState,
                isPasswordVisible = state.isPasswordVisible,
                onToggleVisibilityClick = {
                    onAction(RegisterAction.OnTogglePasswordVisibilityClick)
                },
                title = stringResource(Res.string.password),
                isError = state.passwordError != null,
                supportingText = state.passwordError?.asString() ?: stringResource(Res.string.password_hint)
            )
            Spacer(modifier = Modifier.height(32.dp))

            ChirpButton(
                text = stringResource(Res.string.register),
                onClick = {
                    onAction(RegisterAction.OnRegisterClick)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canRegister,
                isLoading = state.isRegistering
            )
            Spacer(modifier = Modifier.height(8.dp))
            ChirpButton(
                text = stringResource(Res.string.login),
                onClick = {
                    onAction(RegisterAction.OnLoginClick)
                },
                style = ChirpButtonStyle.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ChirpTheme {
        RegisterScreen(
            state = RegisterState(),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}