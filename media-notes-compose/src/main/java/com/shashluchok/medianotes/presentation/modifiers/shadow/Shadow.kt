package com.shashluchok.medianotes.presentation.modifiers.shadow

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shashluchok.medianotes.presentation.utils.toPx
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

private val shadowBlurRadius = 8.dp
private const val shadowAnimationDuration = 250
private const val shadowPaintAlpha = 0.2f

internal enum class ShadowPosition {
    TOP, BOTTOM, LEFT, RIGHT
}

internal fun Modifier.shadow(
    shadowPositions: ImmutableSet<ShadowPosition> = persistentSetOf(
        ShadowPosition.LEFT,
        ShadowPosition.TOP,
        ShadowPosition.RIGHT,
        ShadowPosition.BOTTOM
    ),
    shadowVisible: Boolean = true,
    cornerRadius: Dp = 0.dp
) = composed {
    val blurRadiusPx = shadowBlurRadius.toPx()

    val alpha by animateFloatAsState(
        targetValue = if (shadowVisible) shadowPaintAlpha else 0f,
        animationSpec = tween()
    )

    val shadowPaint = remember {
        createShadowPaint(
            blurRadiusPx,
            alpha
        )
    }

    drawBehind {
        drawIntoCanvas {
            var info = ShadowInfo(
                top = 0f,
                bottom = size.height,
                start = 0f,
                end = size.width
            )

            ShadowPosition.entries
                .filter {
                    shadowPositions.contains(it).not()
                }.onEach { missingShadowPosition ->
                    when (missingShadowPosition) {
                        ShadowPosition.TOP -> {
                            info = info.copy(
                                top = 0f + blurRadiusPx
                            )
                        }

                        ShadowPosition.BOTTOM -> {
                            info = info.copy(
                                bottom = size.height -
                                    blurRadiusPx
                            )
                        }

                        ShadowPosition.LEFT -> {
                            info = info.copy(
                                start = 0f + blurRadiusPx
                            )
                        }

                        ShadowPosition.RIGHT -> {
                            info = info.copy(
                                end = size.width - blurRadiusPx
                            )
                        }
                    }
                }

            it.drawRoundRect(
                left = info.start,
                top = info.top,
                right = info.end,
                bottom = info.bottom,
                paint = shadowPaint.apply {
                    this.alpha = alpha
                },
                radiusX = cornerRadius.toPx(),
                radiusY = cornerRadius.toPx()
            )
        }
    }
}

private fun createShadowPaint(radius: Float, alpha: Float): Paint =
    Paint().apply {
        val frameworkPaint = asFrameworkPaint()
        frameworkPaint.setShadowLayer(radius, 0f, 0f, Color.Black.copy(alpha = alpha).toArgb())
        frameworkPaint.maskFilter =
            (BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL))
    }

private data class ShadowInfo(
    val top: Float,
    val bottom: Float,
    val end: Float,
    val start: Float
)
