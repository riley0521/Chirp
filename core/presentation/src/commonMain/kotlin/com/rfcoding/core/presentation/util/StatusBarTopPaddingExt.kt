package com.rfcoding.core.presentation.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

val statusBarsTopPadding: Dp
    @Composable
    get() = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()