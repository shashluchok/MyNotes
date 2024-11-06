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
import com.shashluchok.medianotes.presentation.toPx
import kotlinx.collections.immutable.ImmutableSet

private val shadowBlurRadius = 8.dp
private const val shadowAnimationDuration = 250
private const val shadowPaintExtraAlpha = 0.3f
private const val shadowPaintAlpha = 0.02f

private const val shadowTranslationLabel = "translation"

internal enum class ShadowPosition {
    TOP, BOTTOM, LEFT, RIGHT
}

internal fun Modifier.shadow(
    shadowPositions: ImmutableSet<ShadowPosition>,
    shadowVisible: Boolean = true,
    cornerRadius: Dp = 0.dp
) = composed {
    val blurRadiusPx = shadowBlurRadius.toPx()
    val shadowPaint = remember {
        createShadowPaint(
            blurRadiusPx
        ).apply { alpha = shadowPaintExtraAlpha }
    }

    val shadowTranslation by animateFloatAsState(
        targetValue = if (shadowVisible) 0f else blurRadiusPx,
        animationSpec = tween(
            durationMillis = shadowAnimationDuration
        ),
        label = shadowTranslationLabel
    )
    drawBehind {
        drawIntoCanvas {
            var info = ShadowInfo(
                top = 0f + shadowTranslation,
                bottom = size.height - shadowTranslation,
                start = 0f + shadowTranslation,
                end = size.width - shadowTranslation

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
                paint = shadowPaint,
                radiusX = cornerRadius.toPx(),
                radiusY = cornerRadius.toPx()
            )
        }
    }
}

private fun createShadowPaint(radius: Float): Paint =
    Paint().apply {
        val frameworkPaint = asFrameworkPaint()
        frameworkPaint.setShadowLayer(radius, 0f, 0f, Color.Black.copy(alpha = shadowPaintAlpha).toArgb())
        frameworkPaint.maskFilter =
            (BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL))
    }

private data class ShadowInfo(
    val top: Float,
    val bottom: Float,
    val end: Float,
    val start: Float
)
