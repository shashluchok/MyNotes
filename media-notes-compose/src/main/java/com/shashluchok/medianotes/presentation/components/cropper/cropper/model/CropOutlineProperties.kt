package com.shashluchok.medianotes.presentation.components.cropper.cropper.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset

@Immutable
internal data class CornerRadiusProperties(
    val topStartPercent: Int = 20,
    val topEndPercent: Int = 20,
    val bottomStartPercent: Int = 20,
    val bottomEndPercent: Int = 20
)

@Immutable
internal data class OvalProperties(
    val startAngle: Float = 0f,
    val sweepAngle: Float = 360f,
    val offset: Offset = Offset.Zero
)
