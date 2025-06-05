package com.shashluchok.medianotes.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CustomRippleColor(color: Color, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        value = LocalRippleConfiguration provides RippleConfiguration(color = color),
        content = content
    )
}
