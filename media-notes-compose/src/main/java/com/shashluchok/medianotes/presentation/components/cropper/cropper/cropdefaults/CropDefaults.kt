package com.shashluchok.medianotes.presentation.components.cropper.cropper.cropdefaults

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.shashluchok.medianotes.presentation.components.cropper.cropper.model.CropAspectRatio
import com.shashluchok.medianotes.presentation.components.cropper.cropper.model.CropShape
import com.shashluchok.medianotes.presentation.components.cropper.cropper.model.RectCropShape
import kotlin.math.roundToInt

private const val defaultHandleSize = 10f
private const val defaultMaxZoom = 5f

private val defaultStrokeWidth = 1.dp

private val DefaultBackgroundColor = Color(0x99000000)
private val DefaultOverlayColor = Color.Gray
private val DefaultHandleColor = Color.White

internal object CropDefaults {
    fun properties(
        handleSize: Float = defaultHandleSize,
        maxZoom: Float = defaultMaxZoom,
        cropShape: CropShape = RectCropShape(),
        aspectRatio: CropAspectRatio? = null,
        minSize: IntSize = IntSize(
            (handleSize * 2).roundToInt(),
            (handleSize * 2).roundToInt()
        )
    ): CropProperties {
        return CropProperties(
            handleSize = handleSize,
            cropShape = cropShape,
            maxZoom = maxZoom,
            aspectRatio = aspectRatio,
            minSize = minSize
        )
    }

    fun style(
        drawGrid: Boolean = true,
        strokeWidth: Dp = defaultStrokeWidth,
        overlayColor: Color = DefaultOverlayColor,
        handleColor: Color = DefaultHandleColor,
        backgroundColor: Color = DefaultBackgroundColor
    ): CropStyle {
        return CropStyle(
            drawGrid = drawGrid,
            strokeWidth = strokeWidth,
            overlayColor = overlayColor,
            handleColor = handleColor,
            backgroundColor = backgroundColor
        )
    }
}

@Immutable
internal data class CropProperties(
    val handleSize: Float,
    val cropShape: CropShape,
    val aspectRatio: CropAspectRatio?,
    val minSize: IntSize,
    val maxZoom: Float
) {
    init {
        require(maxZoom >= 1f)
    }
}

@Immutable
internal data class CropStyle(
    val drawGrid: Boolean,
    val strokeWidth: Dp,
    val overlayColor: Color,
    val handleColor: Color,
    val backgroundColor: Color
)
