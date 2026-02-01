package com.rfcoding.auth.presentation.reset_password

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.feature.auth.presentation.generated.resources.Res
import chirp.feature.auth.presentation.generated.resources.new_password
import chirp.feature.auth.presentation.generated.resources.password
import chirp.feature.auth.presentation.generated.resources.password_hint
import chirp.feature.auth.presentation.generated.resources.reset_password_successfully
import chirp.feature.auth.presentation.generated.resources.set_new_password
import chirp.feature.auth.presentation.generated.resources.submit
import com.rfcoding.core.designsystem.components.brand.ChirpBrandLogo
import com.rfcoding.core.designsystem.components.buttons.ChirpButton
import com.rfcoding.core.designsystem.components.layouts.ChirpAdaptiveFormLayout
import com.rfcoding.core.designsystem.components.textfields.ChirpPasswordTextField
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.presentation.util.UiText
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ResetPasswordRoot(
    onBack: () -> Unit,
    viewModel: ResetPasswordViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ResetPasswordScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun ResetPasswordScreen(
    state: ResetPasswordState,
    onAction: (ResetPasswordAction) -> Unit,
) {
    ChirpAdaptiveFormLayout(
        headerText = stringResource(Res.string.set_new_password),
        logo = {
            ChirpBrandLogo()
        },
        errorText = state.error?.asString()
    ) {
        ChirpPasswordTextField(
            state = state.passwordTextFieldState,
            isPasswordVisible = state.isPasswordVisible,
            onToggleVisibilityClick = {
                onAction(ResetPasswordAction.OnTogglePasswordVisibilityClick)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(Res.string.password),
            title = stringResource(Res.string.new_password),
            supportingText = stringResource(Res.string.password_hint)
        )
        Spacer(modifier = Modifier.height(16.dp))
        ChirpButton(
            text = stringResource(Res.string.submit),
            onClick = {
                onAction(ResetPasswordAction.OnSubmitClick)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.canSubmit && !state.isLoading,
            isLoading = state.isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (state.isResetPasswordSuccessful) {
            Text(
                text = stringResource(Res.string.reset_password_successfully),
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
        ResetPasswordScreen(
            state = ResetPasswordState(
                error = UiText.DynamicText("Invalid token or link expired."),
                canSubmit = true,
                isResetPasswordSuccessful = true
            ),
            onAction = {}
        )
    }
}