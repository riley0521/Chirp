package com.rfcoding.auth.presentation.register_success

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.feature.auth.presentation.generated.resources.Res
import chirp.feature.auth.presentation.generated.resources.account_successfully_created
import chirp.feature.auth.presentation.generated.resources.login
import chirp.feature.auth.presentation.generated.resources.resend_verification_email
import chirp.feature.auth.presentation.generated.resources.resent_verification_email
import chirp.feature.auth.presentation.generated.resources.verification_email_sent_to_x
import com.rfcoding.core.designsystem.components.brand.ChirpSuccessIcon
import com.rfcoding.core.designsystem.components.buttons.ChirpButton
import com.rfcoding.core.designsystem.components.buttons.ChirpButtonStyle
import com.rfcoding.core.designsystem.components.layouts.ChirpAdaptiveResultLayout
import com.rfcoding.core.designsystem.components.layouts.ChirpSimpleResultLayout
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterSuccessRoot(
    onLogin: () -> Unit,
    viewModel: RegisterSuccessViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            RegisterSuccessEvent.Login -> {
                onLogin()
            }

            is RegisterSuccessEvent.ResendEmailVerificationFailure -> {
                snackbarHostState.showSnackbar(message = event.error.asStringAsync())
            }

            RegisterSuccessEvent.ResendEmailVerificationSuccess -> {
                snackbarHostState.showSnackbar(message = getString(Res.string.resent_verification_email))
            }
        }
    }

    RegisterSuccessScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun RegisterSuccessScreen(
    state: RegisterSuccessState,
    onAction: (RegisterSuccessAction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    ChirpAdaptiveResultLayout(
        snackbarHostState = snackbarHostState
    ) {
        ChirpSimpleResultLayout(
            title = stringResource(Res.string.account_successfully_created),
            description = stringResource(
                Res.string.verification_email_sent_to_x,
                state.registeredEmail
            ),
            icon = {
                ChirpSuccessIcon()
            },
            primaryButton = {
                ChirpButton(
                    text = stringResource(Res.string.login),
                    onClick = {
                        onAction(RegisterSuccessAction.OnLoginClick)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            secondaryButton = {
                ChirpButton(
                    text = stringResource(Res.string.resend_verification_email),
                    onClick = {
                        onAction(RegisterSuccessAction.OnResendEmailVerificationClick)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    style = ChirpButtonStyle.SECONDARY,
                    enabled = !state.isResendingEmailVerification,
                    isLoading = state.isResendingEmailVerification
                )
            }
        )
    }
}

@Preview
@Composable
private fun Preview() {
    ChirpTheme {
        RegisterSuccessScreen(
            state = RegisterSuccessState(
                registeredEmail = "hello@chirp.com"
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}