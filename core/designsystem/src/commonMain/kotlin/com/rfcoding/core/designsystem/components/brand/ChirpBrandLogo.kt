package com.rfcoding.core.designsystem.components.brand

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import chirp.core.designsystem.generated.resources.Res
import chirp.core.designsystem.generated.resources.logo_chirp
import com.rfcoding.core.designsystem.theme.ChirpTheme
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ChirpBrandLogo(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Icon(
        imageVector = vectorResource(Res.drawable.logo_chirp),
        contentDescription = null,
        tint = tint,
        modifier = modifier
    )
}

@Composable
@Preview
private fun ChirpBrandLogoPreview() {
    ChirpTheme {
        ChirpBrandLogo()
    }
}