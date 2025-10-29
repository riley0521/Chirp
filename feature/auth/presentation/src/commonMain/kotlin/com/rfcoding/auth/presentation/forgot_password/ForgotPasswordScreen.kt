package com.rfcoding.auth.presentation.forgot_password

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.feature.auth.presentation.generated.resources.Res
import chirp.feature.auth.presentation.generated.resources.email
import chirp.feature.auth.presentation.generated.resources.email_placeholder
import chirp.feature.auth.presentation.generated.resources.forgot_password
import chirp.feature.auth.presentation.generated.resources.forgot_password_email_sent_successfully
import chirp.feature.auth.presentation.generated.resources.submit
import com.rfcoding.core.designsystem.components.brand.ChirpBrandLogo
import com.rfcoding.core.designsystem.components.buttons.ChirpButton
import com.rfcoding.core.designsystem.components.layouts.ChirpAdaptiveFormLayout
import com.rfcoding.core.designsystem.components.textfields.ChirpTextField
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.presentation.util.UiText
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ForgotPasswordRoot(
    viewModel: ForgotPasswordViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ForgotPasswordScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordState,
    onAction: (ForgotPasswordAction) -> Unit,
) {
    ChirpAdaptiveFormLayout(
        headerText = stringResource(Res.string.forgot_password),
        logo = {
            ChirpBrandLogo()
        },
        errorText = state.error?.asString()
    ) {
        ChirpTextField(
            state = state.emailTextFieldState,
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(Res.string.email_placeholder),
            title = stringResource(Res.string.email),
            singleLine = true,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Go,
            onKeyboardAction = {
                onAction(ForgotPasswordAction.OnSubmitClick)
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        ChirpButton(
            text = stringResource(Res.string.submit),
            onClick = {
                onAction(ForgotPasswordAction.OnSubmitClick)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.canSubmit && !state.isLoading,
            isLoading = state.isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (state.isEmailSentSuccessfully) {
            Text(
                text = stringResource(Res.string.forgot_password_email_sent_successfully),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.extended.success,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ChirpTheme {
        ForgotPasswordScreen(
            state = ForgotPasswordState(
                error = UiText.DynamicText("You've hit the rate limit."),
                isEmailSentSuccessfully = true
            ),
            onAction = {}
        )
    }
}