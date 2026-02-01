package com.rfcoding.auth.presentation.login

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.feature.auth.presentation.generated.resources.Res
import chirp.feature.auth.presentation.generated.resources.create_account
import chirp.feature.auth.presentation.generated.resources.email
import chirp.feature.auth.presentation.generated.resources.email_placeholder
import chirp.feature.auth.presentation.generated.resources.forgot_password
import chirp.feature.auth.presentation.generated.resources.login
import chirp.feature.auth.presentation.generated.resources.password
import chirp.feature.auth.presentation.generated.resources.welcome_back
import com.rfcoding.core.designsystem.components.brand.ChirpBrandLogo
import com.rfcoding.core.designsystem.components.buttons.ChirpButton
import com.rfcoding.core.designsystem.components.buttons.ChirpButtonStyle
import com.rfcoding.core.designsystem.components.layouts.ChirpAdaptiveFormLayout
import com.rfcoding.core.designsystem.components.textfields.ChirpPasswordTextField
import com.rfcoding.core.designsystem.components.textfields.ChirpTextField
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.presentation.util.ObserveAsEvents
import com.rfcoding.core.presentation.util.UiText
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginRoot(
    onForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    onRegister: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when(event) {
            LoginEvent.ForgotPassword -> {
                onForgotPassword()
            }
            LoginEvent.LoginSuccessful -> {
                onLoginSuccess()
            }
            LoginEvent.Register -> {
                onRegister()
            }
        }
    }

    LoginScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
) {
    ChirpAdaptiveFormLayout(
        headerText = stringResource(Res.string.welcome_back),
        logo = {
            ChirpBrandLogo()
        },
        errorText = state.error?.asString()
    ) {
        ChirpTextField(
            state = state.emailTextFieldState,
            placeholder = stringResource(Res.string.email_placeholder),
            title = stringResource(Res.string.email),
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        ChirpPasswordTextField(
            state = state.passwordTextFieldState,
            isPasswordVisible = state.isPasswordVisible,
            onToggleVisibilityClick = {
                onAction(LoginAction.OnTogglePasswordVisibility)
            },
            title = stringResource(Res.string.password),
            imeAction = ImeAction.Go,
            onKeyboardGo = {
                onAction(LoginAction.OnLoginClick)
            },
            modifier = Modifier.fillMaxWidth()
        )
        ChirpButton(
            text = stringResource(Res.string.forgot_password),
            onClick = {
                onAction(LoginAction.OnForgotPasswordClick)
            },
            style = ChirpButtonStyle.TEXT,
            modifier = Modifier.align(Alignment.End)
        )
        Spacer(modifier = Modifier.height(32.dp))
        ChirpButton(
            text = stringResource(Res.string.login),
            onClick = {
                onAction(LoginAction.OnLoginClick)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.canLogin,
            isLoading = state.isLoggingIn
        )
        Spacer(modifier = Modifier.height(8.dp))
        ChirpButton(
            text = stringResource(Res.string.create_account),
            onClick = {
                onAction(LoginAction.OnRegisterClick)
            },
            modifier = Modifier.fillMaxWidth(),
            style = ChirpButtonStyle.SECONDARY
        )
    }
}

@Preview
@Composable
private fun LoginScreenLightPreview() {
    ChirpTheme {
        LoginScreen(
            state = LoginState(
                error = UiText.DynamicText("Invalid credentials")
            ),
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun LoginScreenDarkPreview() {
    ChirpTheme(darkTheme = true) {
        LoginScreen(
            state = LoginState(
                error = UiText.DynamicText("Invalid credentials"),
                canLogin = true
            ),
            onAction = {}
        )
    }
}