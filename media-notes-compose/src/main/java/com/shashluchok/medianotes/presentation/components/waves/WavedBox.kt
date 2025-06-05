package com.shashluchok.medianotes.presentation.components.waves

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath

private const val voiceAnimatedShapesSizeMultiplier = 5f
private const val voiceBackgroundCircleSizeMultiplier = 3f
private const val voiceIconBackgroundStartShapeVerticesCount = 6
private val voiceIconBackgroundStartShapeRounding = CornerRounding(0.70f)
private const val voiceIconBackgroundEndShapeVerticesCount = 24
private val voiceIconBackgroundEndShapeRounding = CornerRounding(1f)
private const val voiceIconBackgroundShapesColorAlpha = 0.1f

private const val voiceIconBackgroundMorphAnimationDuration = 1500

private const val voiceIconBackgroundRotateAnimationDuration = 2500

private const val voiceIconBackgroundScaleAnimationDuration = 300

private const val outerShapeScaleMultiplier = 3f
private const val innerShapeScaleMultiplier = 1.5f
private const val shapeScaleAnimationDurationMillis = 150

@Composable
internal fun WavedBox(
    visible: Boolean,
    volumeLevel: Float,
    modifier: Modifier = Modifier,
    wavesSizeMultiplier: Float = voiceAnimatedShapesSizeMultiplier,
    circleSizeMultiplier: Float = voiceBackgroundCircleSizeMultiplier,
    content: @Composable BoxScope.() -> Unit
) {
    val onDragBackgroundColor = MaterialTheme.colorScheme.primary

    val startShape = remember {
        RoundedPolygon(
            numVertices = voiceIconBackgroundStartShapeVerticesCount,
            rounding = voiceIconBackgroundStartShapeRounding
        )
    }
    val endShape = remember {
        RoundedPolygon(
            numVertices = voiceIconBackgroundEndShapeVerticesCount,
            rounding = voiceIconBackgroundEndShapeRounding
        )
    }

    val morph = remember {
        Morph(startShape, endShape)
    }

    val infiniteTransition = rememberInfiniteTransition()

    val morphProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(
                durationMillis = voiceIconBackgroundMorphAnimationDuration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotateProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(voiceIconBackgroundRotateAnimationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val outerShapeScale = remember { Animatable(1f) }

    LaunchedEffect(volumeLevel) {
        outerShapeScale.animateTo(
            targetValue = volumeLevel / outerShapeScaleMultiplier + 1f,
            animationSpec = tween(durationMillis = shapeScaleAnimationDurationMillis)
        )
    }

    val innerShapeScale = remember { Animatable(1f) }

    LaunchedEffect(volumeLevel) {
        innerShapeScale.animateTo(
            targetValue = volumeLevel / innerShapeScaleMultiplier + 1f,
            animationSpec = tween(durationMillis = shapeScaleAnimationDurationMillis)
        )
    }

    val matrix = remember {
        Matrix()
    }

    val displayScale by animateFloatAsState(
        targetValue = if (visible) {
            1f
        } else {
            0f
        },
        animationSpec = tween(
            durationMillis = voiceIconBackgroundScaleAnimationDuration
        )
    )

    Box(
        modifier = modifier.drawWithCache {
            matrix.reset()
            matrix.translate(size.minDimension / 2, size.minDimension / 2)
            matrix.scale(
                x = size.minDimension / 2 * wavesSizeMultiplier,
                y = size.minDimension / 2 * wavesSizeMultiplier
            )

            onDrawBehind {
                scale(displayScale) {
                    scale(outerShapeScale.value) {
                        rotate(rotateProgress) {
                            drawPath(
                                path = morph.toComposePath(
                                    progress = morphProgress,
                                    matrix = matrix
                                ),
                                color = onDragBackgroundColor.copy(
                                    alpha = voiceIconBackgroundShapesColorAlpha
                                )
                            )
                        }
                        rotate(-rotateProgress) {
                            drawPath(
                                path = morph.toComposePath(
                                    // Reversed animation
                                    progress = 1 - morphProgress,
                                    matrix = matrix
                                ),
                                color = onDragBackgroundColor.copy(
                                    alpha = voiceIconBackgroundShapesColorAlpha
                                )
                            )
                        }
                    }
                    scale(innerShapeScale.value) {
                        drawCircle(
                            color = onDragBackgroundColor,
                            radius = size.minDimension / 2 * circleSizeMultiplier
                        )
                    }
                }
            }
        },
        content = content
    )
}

private fun Morph.toComposePath(
    progress: Float,
    matrix: Matrix
): Path = toPath(progress).asComposePath().also {
    it.transform(matrix)
}
