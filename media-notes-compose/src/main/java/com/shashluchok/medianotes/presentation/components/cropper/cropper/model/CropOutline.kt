package com.shashluchok.medianotes.presentation.components.cropper.cropper.model

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape

internal interface CropShape {
    val shape: Shape
}

@Immutable
internal data class RectCropShape(
    override val shape: Shape = RectangleShape
) : CropShape

@Immutable
internal data class RoundedCornerCropShape(
    val cornerRadius: CornerRadiusProperties = CornerRadiusProperties(),
    override val shape: RoundedCornerShape = RoundedCornerShape(
        topStartPercent = cornerRadius.topStartPercent,
        topEndPercent = cornerRadius.topEndPercent,
        bottomEndPercent = cornerRadius.bottomEndPercent,
        bottomStartPercent = cornerRadius.bottomStartPercent
    )
) : CropShape

@Immutable
internal data class OvalCropShape(
    val ovalProperties: OvalProperties = OvalProperties(),
    override val shape: Shape = CircleShape
) : CropShape
