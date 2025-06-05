package com.shashluchok.medianotes.presentation.screen.sketch

import android.graphics.Canvas
import android.graphics.Color.HSVToColor
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.shashluchok.medianotes.presentation.modifiers.pointerinput.detectPressAndDrag
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private val selectionCircleBorderWidth = 2.dp
private val selectionCircleInnerRadius = 2.dp
private val selectionCircleOuterRadius = 8.dp

private val borderWidth = 1.dp
private const val borderAlpha = 0.5f

@Composable
internal fun SaturationPanel(
    radius: Dp,
    hue: Float,
    currentSaturation: Float,
    currentValue: Float,
    onSaturationAndValueChange: (saturation: Float, value: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .detectPressAndDrag { eventOffset ->
                val (newSaturation, newValue) = pointToSaturationAndValue(point = eventOffset)
                onSaturationAndValueChange(newSaturation, newValue)
            }
            .drawWithContent {
                drawContent()
                drawSelectionCircle(
                    currentSaturation = currentSaturation,
                    currentValue = currentValue,
                    radius = radius
                )
            }
            .border(
                width = borderWidth,
                color = MaterialTheme.colorScheme.outlineVariant.copy(
                    alpha = borderAlpha
                ),
                shape = RoundedCornerShape(radius)
            )
            .clip(RoundedCornerShape(radius)),
        onDraw = { drawSaturationPanelBitmap(hue) }
    )
}

private fun DrawScope.drawSaturationPanelBitmap(
    hue: Float
) {
    val bitmap = createBitmap(size.width.toInt(), size.height.toInt())
    val canvas = Canvas(bitmap)
    val saturationRect = RectF(
        0f,
        0f,
        bitmap.width.toFloat(),
        bitmap.height.toFloat()
    )

    val rgb = HSVToColor(floatArrayOf(hue, 1f, 1f))

    val satShader = LinearGradient(
        saturationRect.left,
        saturationRect.top,
        saturationRect.right,
        saturationRect.top,
        -0x1,
        rgb,
        Shader.TileMode.CLAMP
    )
    val valShader = LinearGradient(
        saturationRect.left,
        saturationRect.top,
        saturationRect.left,
        saturationRect.bottom,
        -0x1,
        -0x1000000,
        Shader.TileMode.CLAMP
    )

    canvas.drawRect(
        saturationRect,
        Paint().apply {
            shader = ComposeShader(
                valShader,
                satShader,
                PorterDuff.Mode.MULTIPLY
            )
        }
    )
    drawIntoCanvas {
        it.nativeCanvas.drawBitmap(
            bitmap,
            0f,
            0f,
            null
        )
    }
}

private fun DrawScope.drawSelectionCircle(
    currentSaturation: Float,
    currentValue: Float,
    radius: Dp
) {
    val selectionX = currentSaturation * size.width
    val selectionY = (1f - currentValue) * size.height

    val currentOffset = Offset(selectionX, selectionY).coerceInRoundedRect(
        radius = radius.toPx(),
        size = size
    )

    drawCircle(
        color = Color.White,
        radius = selectionCircleOuterRadius.toPx(),
        center = currentOffset,
        style = Stroke(width = selectionCircleBorderWidth.toPx())
    )

    drawCircle(
        color = Color.White,
        radius = selectionCircleInnerRadius.toPx(),
        center = currentOffset
    )
}

private fun Offset.coerceInRoundedRect(
    radius: Float,
    size: Size
): Offset {
    fun Offset.distanceTo(other: Offset): Float {
        return sqrt((other.x - x).pow(2) + (other.y - y).pow(2))
    }

    val width = size.width
    val height = size.height

    var pointX = x.coerceIn(0f, width)
    var pointY = y.coerceIn(0f, height)

    if (pointX < radius && pointY < radius) {
        val corner = Offset(radius, radius)
        val distance = Offset(pointX, pointY).distanceTo(corner)

        if (distance > radius) {
            val angle = atan2(pointY - radius, pointX - radius)
            pointX = radius + radius * cos(angle)
            pointY = radius + radius * sin(angle)
        }
    }

    if (pointX > width - radius && pointY < radius) {
        val corner = Offset(width - radius, radius)
        val distance = Offset(pointX, pointY).distanceTo(corner)

        if (distance > radius) {
            val angle = atan2(pointY - radius, pointX - (width - radius))
            pointX = (width - radius) + radius * cos(angle)
            pointY = radius + radius * sin(angle)
        }
    }

    if (pointX < radius && pointY > height - radius) {
        val corner = Offset(radius, height - radius)
        val distance = Offset(pointX, pointY).distanceTo(corner)

        if (distance > radius) {
            val angle = atan2(pointY - (height - radius), pointX - radius)
            pointX = radius + radius * cos(angle)
            pointY = (height - radius) + radius * sin(angle)
        }
    }

    // check bottom right angle
    if (pointX > width - radius && pointY > height - radius) {
        val corner = Offset(width - radius, height - radius)
        val distance = Offset(pointX, pointY).distanceTo(corner)

        if (distance > radius) {
            val angle = atan2(pointY - (height - radius), pointX - (width - radius))
            pointX = (width - radius) + radius * cos(angle)
            pointY = (height - radius) + radius * sin(angle)
        }
    }

    return Offset(pointX, pointY)
}

private fun PointerInputScope.pointToSaturationAndValue(
    point: Offset
): Pair<Float, Float> {
    val width = size.width.toFloat()
    val height = size.height.toFloat()

    val pointX = point.x.coerceIn(0f..size.width.toFloat())
    val pointY = point.y.coerceIn(0f..size.height.toFloat())

    val saturation = (1f / width) * pointX
    val value = 1f - (1f / height) * pointY

    return saturation to value
}
