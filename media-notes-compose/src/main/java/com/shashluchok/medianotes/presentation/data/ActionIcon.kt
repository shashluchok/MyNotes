package com.shashluchok.medianotes.presentation.data

import androidx.compose.material3.IconButtonColors
import androidx.compose.ui.graphics.painter.Painter

internal data class ActionIcon(
    val painter: Painter,
    val onClick: () -> Unit,
    val colors: IconButtonColors? = null,
    val contentDescription: String? = null,
    val enabled: Boolean = true
)
