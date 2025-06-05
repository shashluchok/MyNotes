package com.shashluchok.medianotes.presentation.screen.sketch

import android.annotation.SuppressLint
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

private const val pathAnimationDuration = 2000

private const val pathStartXMultiplier = 0.2f
private const val pathStartYMultiplier = 0.6f
private const val pathEndXMultiplier = 0.8f
private const val pathEndYMultiplier = 0.6f

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
internal fun DrawPreviewBox(
    drawColor: Color,
    drawThickness: Dp,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        val density = LocalDensity.current

        val previewPathMeasure = remember(maxWidth, maxHeight) {
            with(density) {
                getPreviewPathMeasure(maxWidth.toPx(), maxHeight.toPx())
            }
        }

        val transition = rememberInfiniteTransition()

        val pathAnimation by transition.animateFloat(
            // to prevent blinking
            initialValue = 0.01f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = pathAnimationDuration,
                    easing = EaseInOut
                ),
                repeatMode = RepeatMode.Reverse
            )
        )

        val animatedPath by remember {
            derivedStateOf {
                val newPath = Path()
                previewPathMeasure.getSegment(
                    startDistance = 0f,
                    stopDistance = pathAnimation * previewPathMeasure.length,
                    destination = newPath,
                    startWithMoveTo = true
                )
                newPath
            }
        }

        Box(
            modifier = Modifier.drawWithContent {
                drawContent()
                drawPath(
                    path = animatedPath,
                    color = drawColor,
                    style = Stroke(
                        width = drawThickness.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

        )
    }
}

private fun getPreviewPathMeasure(
    maxWidth: Float,
    maxHeight: Float
) = PathMeasure().apply {
    val firstPoint = Offset(
        x = maxWidth * pathStartXMultiplier,
        y = maxHeight * pathStartYMultiplier
    )
    val secondPoint = Offset(
        x = maxWidth * pathEndXMultiplier,
        y = maxHeight * pathEndYMultiplier
    )
    setPath(
        path = Path().apply {
            moveTo(firstPoint.x, firstPoint.y)
            cubicTo(
                x1 = firstPoint.x,
                y1 = firstPoint.y,
                x2 = firstPoint.x + (secondPoint.x - firstPoint.x) / 2,
                y2 = 0f,
                x3 = secondPoint.x,
                y3 = secondPoint.y
            )
        },
        forceClosed = false
    )
}
