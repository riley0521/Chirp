package com.rfcoding.core.designsystem.components.layouts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rfcoding.core.designsystem.components.brand.ChirpBrandLogo
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.presentation.util.DeviceConfiguration
import com.rfcoding.core.presentation.util.clearFocusOnTap
import com.rfcoding.core.presentation.util.currentDeviceConfiguration

@Composable
fun ChirpAdaptiveFormLayout(
    headerText: String,
    logo: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    snackbarHostState: SnackbarHostState? = null,
    formContent: @Composable ColumnScope.() -> Unit
) {
    val configuration = currentDeviceConfiguration()
    val headerColor = if (configuration == DeviceConfiguration.MOBILE_LANDSCAPE) {
        MaterialTheme.colorScheme.onBackground
    } else MaterialTheme.colorScheme.extended.textPrimary

    ChirpSnackbarScaffold(
        snackbarHostState = snackbarHostState
    ) {
        when (configuration) {
            DeviceConfiguration.MOBILE_PORTRAIT -> {
                ChirpSurface(
                    modifier = modifier
                        .clearFocusOnTap()
                        .consumeWindowInsets(WindowInsets.navigationBars)
                        .consumeWindowInsets(WindowInsets.displayCutout),
                    header = {
                        Spacer(modifier = Modifier.height(32.dp))
                        logo()
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    AuthHeaderSection(
                        headerText = headerText,
                        headerColor = headerColor,
                        errorText = errorText
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    formContent()
                }
            }
            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = modifier
                        .clearFocusOnTap()
                        .fillMaxSize()
                        .consumeWindowInsets(WindowInsets.displayCutout)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .padding(WindowInsets
                            .navigationBars
                            .only(WindowInsetsSides.Horizontal).asPaddingValues())
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        logo()
                        AuthHeaderSection(
                            headerText = headerText,
                            headerColor = headerColor,
                            errorText = errorText,
                            textAlign = TextAlign.Start
                        )
                    }
                    ChirpSurface(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        formContent()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            DeviceConfiguration.TABLET_PORTRAIT,
            DeviceConfiguration.TABLET_LANDSCAPE,
            DeviceConfiguration.DESKTOP -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    logo()
                    Column(
                        modifier = Modifier
                            .widthIn(max = 480.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(32.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AuthHeaderSection(
                            headerText = headerText,
                            headerColor = headerColor,
                            errorText = errorText
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        formContent()
                    }
                }
            }
        }
    }

}

@Composable
fun ColumnScope.AuthHeaderSection(
    headerText: String,
    headerColor: Color,
    errorText: String? = null,
    textAlign: TextAlign = TextAlign.Center
) {
    Text(
        text = headerText,
        style = MaterialTheme.typography.titleLarge,
        color = headerColor,
        textAlign = textAlign,
        modifier = Modifier.fillMaxWidth()
    )
    AnimatedVisibility(
        visible = errorText != null
    ) {
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = textAlign,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
@Preview
private fun ChirpAdaptiveFormLayoutDarkPreview() {
    ChirpTheme(
        darkTheme = true
    ) {
        ChirpAdaptiveFormLayout(
            headerText = "Welcome to Chirp!",
            errorText = "Login failed.",
            logo = {
                ChirpBrandLogo()
            },
            formContent = {
                Text(
                    text = "Nice galing!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        )
    }
}