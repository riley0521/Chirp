package com.rfcoding.auth.presentation.register

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.rfcoding.core.designsystem.components.textfields.ChirpPasswordTextField
import com.rfcoding.core.designsystem.components.textfields.ChirpTextField
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterRoot(
    onRegisterSuccess: (String) -> Unit,
    onLogin: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is RegisterEvent.Success -> {
                onRegisterSuccess(event.email)
            }

            RegisterEvent.Login -> {
                onLogin()
            }
        }
    }

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
    ChirpAdaptiveFormLayout(
        headerText = stringResource(Res.string.welcome_to_chirp),
        logo = {
            ChirpBrandLogo()
        },
        errorText = state.registrationError?.asString(),
        snackbarHostState = snackbarHostState
    ) {
        ChirpTextField(
            state = state.usernameTextState,
            title = stringResource(Res.string.username),
            placeholder = stringResource(Res.string.username_placeholder),
            isError = state.usernameError != null,
            supportingText = state.usernameError?.asString() ?: stringResource(Res.string.username_hint),
            imeAction = ImeAction.Next
        )
        Spacer(modifier = Modifier.height(20.dp))
        ChirpTextField(
            state = state.emailTextState,
            title = stringResource(Res.string.email),
            placeholder = stringResource(Res.string.email_placeholder),
            isError = state.emailError != null,
            supportingText = state.emailError?.asString(),
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
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
            supportingText = state.passwordError?.asString() ?: stringResource(Res.string.password_hint),
            imeAction = ImeAction.Go,
            onKeyboardGo = {
                onAction(RegisterAction.OnRegisterClick)
            }
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