package com.shashluchok.medianotes.presentation.components.cropper.cropper.draw

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.shashluchok.medianotes.presentation.components.cropper.cropper.model.CropShape
import com.shashluchok.medianotes.presentation.utils.drawGrid
import com.shashluchok.medianotes.presentation.utils.drawWithLayer

@Composable
internal fun DrawingOverlay(
    rect: Rect,
    cropShape: CropShape,
    drawGrid: Boolean,
    transparentColor: Color,
    overlayColor: Color,
    handleColor: Color,
    strokeWidth: Dp,
    handleSize: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val layoutDirection: LayoutDirection = LocalLayoutDirection.current

    val strokeWidthPx = LocalDensity.current.run { strokeWidth.toPx() }

    val pathHandles = remember {
        Path()
    }
    val outline = remember(rect, cropShape) {
        cropShape.shape.createOutline(rect.size, layoutDirection, density)
    }

    DrawingOverlayImpl(
        modifier = modifier,
        rect = rect,
        drawGrid = drawGrid,
        transparentColor = transparentColor,
        overlayColor = overlayColor,
        handleColor = handleColor,
        strokeWidth = strokeWidthPx,
        handleSize = handleSize,
        pathHandles = pathHandles,
        outline = outline
    )
}

@Composable
private fun DrawingOverlayImpl(
    rect: Rect,
    drawGrid: Boolean,
    transparentColor: Color,
    overlayColor: Color,
    handleColor: Color,
    strokeWidth: Float,
    handleSize: Float,
    pathHandles: Path,
    outline: Outline,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawOverlay(
            rect,
            drawGrid,
            transparentColor,
            overlayColor,
            handleColor,
            strokeWidth,
            handleSize,
            pathHandles
        ) {
            drawCropOutline(outline = outline)
        }
    }
}

private fun DrawScope.drawOverlay(
    rect: Rect,
    drawGrid: Boolean,
    transparentColor: Color,
    overlayColor: Color,
    handleColor: Color,
    strokeWidth: Float,
    handleSize: Float,
    pathHandles: Path,
    drawBlock: DrawScope.() -> Unit
) {
    drawWithLayer {
        // Destination
        drawRect(transparentColor)

        // Source
        translate(left = rect.left, top = rect.top) {
            drawBlock()
        }

        if (drawGrid) {
            drawGrid(
                rect = rect,
                strokeWidth = strokeWidth,
                color = overlayColor
            )
        }
    }

    drawRect(
        topLeft = rect.topLeft,
        size = rect.size,
        color = overlayColor,
        style = Stroke(width = strokeWidth)
    )

    pathHandles.apply {
        reset()
        updateHandlePath(rect, handleSize)
    }

    drawPath(
        path = pathHandles,
        color = handleColor,
        style = Stroke(
            width = strokeWidth * 2,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

private fun DrawScope.drawCropOutline(
    outline: Outline,
    blendMode: BlendMode = BlendMode.SrcOut
) {
    drawOutline(
        outline = outline,
        color = Color.Transparent,
        blendMode = blendMode
    )
}

private fun Path.updateHandlePath(
    rect: Rect,
    handleSize: Float
) {
    if (rect != Rect.Zero) {
        // Top left lines
        moveTo(rect.topLeft.x, rect.topLeft.y + handleSize)
        lineTo(rect.topLeft.x, rect.topLeft.y)
        lineTo(rect.topLeft.x + handleSize, rect.topLeft.y)

        // Top right lines
        moveTo(rect.topRight.x - handleSize, rect.topRight.y)
        lineTo(rect.topRight.x, rect.topRight.y)
        lineTo(rect.topRight.x, rect.topRight.y + handleSize)

        // Bottom right lines
        moveTo(rect.bottomRight.x, rect.bottomRight.y - handleSize)
        lineTo(rect.bottomRight.x, rect.bottomRight.y)
        lineTo(rect.bottomRight.x - handleSize, rect.bottomRight.y)

        // Bottom left lines
        moveTo(rect.bottomLeft.x + handleSize, rect.bottomLeft.y)
        lineTo(rect.bottomLeft.x, rect.bottomLeft.y)
        lineTo(rect.bottomLeft.x, rect.bottomLeft.y - handleSize)
    }
}
