package com.shashluchok.medianotes.presentation.screen.sketch

import android.graphics.Canvas
import android.graphics.Color.HSVToColor
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.shashluchok.medianotes.presentation.modifiers.pointerinput.detectPressAndDrag

private const val selectionCircleSizeMultiplier = 1.5f
private val selectionCircleStrokeWidth = 2.dp

private const val hueMaxValue = 360f

private val borderWidth = 1.dp
private const val borderAlpha = 0.5f

@Composable
internal fun HuePanel(
    currentHue: Float,
    onHueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .detectPressAndDrag { offset ->
                val pressOffsetX = offset.x.coerceIn(
                    range = 0f..size.width.toFloat()
                )
                val selectedHue = (pressOffsetX / size.width) * hueMaxValue
                onHueChange(selectedHue)
            }
            .drawWithContent {
                drawContent()
                drawSelectionCircle(selectedHue = currentHue)
            }
            .border(
                width = borderWidth,
                color = MaterialTheme.colorScheme.outlineVariant.copy(
                    alpha = borderAlpha
                ),
                shape = CircleShape
            )
            .clip(CircleShape),
        onDraw = { drawHuePanelBitmap() }
    )
}

private fun DrawScope.drawHuePanelBitmap() {
    val bitmap = createBitmap(size.width.toInt(), size.height.toInt())
    val hueCanvas = Canvas(bitmap)

    val hueColors = IntArray(bitmap.width)
    var hue = 0f
    for (i in hueColors.indices) {
        hueColors[i] = HSVToColor(floatArrayOf(hue, 1f, 1f))
        hue += hueMaxValue / hueColors.size
    }

    val linePaint = Paint().apply { strokeWidth = 0f }
    for (i in hueColors.indices) {
        linePaint.color = hueColors[i]
        hueCanvas.drawLine(
            /*startX*/ i.toFloat(),
            /*startY*/0F,
            /*stopX*/ i.toFloat(),
            /*stopY*/bitmap.height.toFloat(),
            /*pain*/linePaint
        )
    }
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
    selectedHue: Float
) {
    fun hueToOffsetX(
        hue: Float,
        width: Float
    ) = (hue / hueMaxValue) * width

    drawCircle(
        color = Color.hsv(selectedHue, 1f, 1f),
        radius = size.height / 2 * selectionCircleSizeMultiplier,
        center = center.copy(
            x = hueToOffsetX(
                hue = selectedHue,
                width = size.width
            )
        )
    )
    drawCircle(
        color = Color.White,
        radius = size.height / 2 * selectionCircleSizeMultiplier,
        center = center.copy(
            x = hueToOffsetX(
                hue = selectedHue,
                width = size.width
            )
        ),
        style = Stroke(width = selectionCircleStrokeWidth.toPx())
    )
}
