package com.shashluchok.medianotes.presentation.components.mediatoolbar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath

private const val voiceAnimatedShapesSizeMultiplier = 2.5f
private const val voiceBackgroundCircleSizeMultiplier = 1.5f
private const val voiceIconBackgroundStartShapeVerticesCount = 6
private val voiceIconBackgroundStartShapeRounding = CornerRounding(0.70f)
private const val voiceIconBackgroundEndShapeVerticesCount = 24
private val voiceIconBackgroundEndShapeRounding = CornerRounding(1f)
private const val voiceIconBackgroundShapesColorAlpha = 0.1f

private const val voiceIconInfiniteTransitionLabel = "voiceInfiniteTransition"

private const val voiceIconBackgroundMorphAnimationDuration = 1500
private const val voiceIconBackgroundMorphAnimationLabel = "voiceAnimatedMorphProgress"

private const val voiceIconBackgroundRotateAnimationDuration = 4500
private const val voiceIconBackgroundRotateAnimationLabel = "voiceAnimatedRotationProgress"

private const val voiceIconBackgroundScaleAnimationDuration = 300
private const val voiceIconBackgroundScaleAnimationLabel = "voiceAnimatedScaleProgress"

private const val voiceRecordingIconTintAnimationLabel = "iconTintAnimation"
private const val voiceRecordingIconTintAnimationDuration = 100
private const val voiceRecordingIconTintAnimationDelay = 100

@Composable
internal fun VoiceToolbarIcon(
    onClick: () -> Unit,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val onDragBackgroundColor = MaterialTheme.colorScheme.primary
    val iconTintColor by animateColorAsState(
        targetValue = if (isRecording) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.primary
        },
        label = voiceRecordingIconTintAnimationLabel,
        animationSpec = tween(
            durationMillis = voiceRecordingIconTintAnimationDuration,
            delayMillis = voiceRecordingIconTintAnimationDelay
        )
    )

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
    val infiniteTransition = rememberInfiniteTransition(label = voiceIconInfiniteTransitionLabel)

    val morphProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(
                durationMillis = voiceIconBackgroundMorphAnimationDuration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = voiceIconBackgroundMorphAnimationLabel
    )

    val rotateProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(voiceIconBackgroundRotateAnimationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = voiceIconBackgroundRotateAnimationLabel
    )

    val scale by animateFloatAsState(
        targetValue = if (isRecording) 1f else 0f,
        animationSpec = tween(
            durationMillis = voiceIconBackgroundScaleAnimationDuration
        ),
        label = voiceIconBackgroundScaleAnimationLabel
    )

    val matrix = remember {
        Matrix()
    }

    ToolbarIcon(
        modifier = modifier
            .testTag(tag = VoiceToolbarIcon.Tag.root)
            .drawWithCache {
                matrix.reset()
                matrix.translate(size.minDimension / 2, size.minDimension / 2)
                matrix.scale(
                    x = size.minDimension * voiceAnimatedShapesSizeMultiplier,
                    y = size.minDimension * voiceAnimatedShapesSizeMultiplier
                )

                onDrawBehind {
                    scale(scale) {
                        rotate(rotateProgress) {
                            drawPath(
                                path = morph.toComposePath(
                                    progress = morphProgress,
                                    matrix = matrix
                                ),
                                color = onDragBackgroundColor.copy(alpha = voiceIconBackgroundShapesColorAlpha)
                            )
                        }
                        rotate(-rotateProgress) {
                            drawPath(
                                path = morph.toComposePath(
                                    // Reversed animation
                                    progress = 1 - morphProgress,
                                    matrix = matrix
                                ),
                                color = onDragBackgroundColor.copy(alpha = voiceIconBackgroundShapesColorAlpha)
                            )
                        }

                        drawCircle(
                            color = onDragBackgroundColor,
                            radius = size.minDimension * voiceBackgroundCircleSizeMultiplier
                        )
                    }
                }
            },
        painter = rememberVectorPainter(Icons.Outlined.Mic),
        tint = iconTintColor,
        onClick = onClick
    )
}

private fun Morph.toComposePath(
    progress: Float,
    matrix: Matrix
): Path = toPath(progress).asComposePath().also {
    it.transform(matrix)
}

object VoiceToolbarIcon {
    object Tag {
        const val root = "VoiceToolbarIcon"
    }
}
