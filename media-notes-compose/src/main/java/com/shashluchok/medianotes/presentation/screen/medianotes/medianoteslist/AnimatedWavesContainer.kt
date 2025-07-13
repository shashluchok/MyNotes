package com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath

private const val wavesScaleInAnimationDuration = 900

private val wavesGroupOuterShapeFirstScaleRange = 0.75f..0.95f
private val wavesGroupOuterShapeFirstAlphaRange = 0.25f..0.35f

private val wavesGroupOuterShapeSecondScaleRange = 1f..1.15f
private val wavesGroupOuterShapeSecondAlphaRange = 0.15f..0.25f

private val wavesGroupInnerShapeFirstScaleRange = 0.4f..0.45f
private val wavesGroupInnerShapeFirstAlphaRange = 0.85f..1f

private val wavesGroupInnerShapeSecondScaleRange = 0.39f..0.46f
private val wavesGroupInnerShapeSecondAlphaRange = 0.35f..0.45f

private val wavesGroupInnerShapeThirdScaleRange = 0.38f..0.4f
private val wavesGroupInnerShapeThirdAlphaRange = 0.25f..0.35f

private val wavesGroupInnerShapeFourthScaleRange = 0.37f..0.41f
private val wavesGroupInnerShapeFourthAlphaRange = 0.25f..0.35f

private const val wavesMorphAnimationDuration = 7000
private const val wavesRotationAnimationDuration = 120000
private const val wavesScaleAnimationDuration = 30000
private const val wavesAlphaAnimationDuration = 10000

private const val fullCircleDegrees = 360f

private const val wavesStartShapeVerticesCount = 7
private const val wavesEndShapeVerticesCount = 6
private const val wavesShapeSmoothing = 0f
private const val wavesShapeRadiusMultiplier = 2f
private const val wavesShapeInnerRadiusMultiplier = 3f
private const val wavesShapeCornerRadiusMultiplier = 7f

private const val wavesShapeStrokeWidth = 2f

private data class WaveAnimationInfo(
    val scaleRange: ClosedFloatingPointRange<Float>,
    val clockWiseRotation: Boolean,
    val alphaRange: ClosedFloatingPointRange<Float>
)

@Composable
internal fun AnimatedWavesContainer(
    wavesVisible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
    ) {
        AnimatedVisibility(
            visible = wavesVisible,
            enter = fadeIn(tween()) + scaleIn(tween(durationMillis = wavesScaleInAnimationDuration)),
            exit = fadeOut(tween())
        ) {
            WavesGroupBox()
        }
        content()
    }
}

@Composable
private fun WavesGroupBox(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AnimatedWaveBox(
            info = WaveAnimationInfo(
                scaleRange = wavesGroupOuterShapeFirstScaleRange,
                clockWiseRotation = true,
                alphaRange = wavesGroupOuterShapeFirstAlphaRange
            )
        )

        AnimatedWaveBox(
            info = WaveAnimationInfo(
                scaleRange = wavesGroupOuterShapeSecondScaleRange,
                clockWiseRotation = false,
                alphaRange = wavesGroupOuterShapeSecondAlphaRange
            )
        )

        AnimatedWaveBox(
            info = WaveAnimationInfo(
                scaleRange = wavesGroupOuterShapeFirstScaleRange,
                clockWiseRotation = true,
                alphaRange = wavesGroupOuterShapeFirstAlphaRange
            )
        )
        AnimatedWaveBox(
            info = WaveAnimationInfo(
                scaleRange = wavesGroupInnerShapeFirstScaleRange,
                clockWiseRotation = false,
                alphaRange = wavesGroupInnerShapeFirstAlphaRange
            )
        )
        AnimatedWaveBox(
            info = WaveAnimationInfo(
                scaleRange = wavesGroupInnerShapeSecondScaleRange,
                clockWiseRotation = true,
                alphaRange = wavesGroupInnerShapeSecondAlphaRange

            )
        )
        AnimatedWaveBox(
            info = WaveAnimationInfo(
                scaleRange = wavesGroupInnerShapeThirdScaleRange,
                clockWiseRotation = false,
                alphaRange = wavesGroupInnerShapeThirdAlphaRange

            )
        )
        AnimatedWaveBox(
            info = WaveAnimationInfo(
                scaleRange = wavesGroupInnerShapeFourthScaleRange,
                clockWiseRotation = true,
                alphaRange = wavesGroupInnerShapeFourthAlphaRange
            )
        )
    }
}

@Composable
private fun AnimatedWaveBox(
    info: WaveAnimationInfo,
    modifier: Modifier = Modifier
) {
    val infiniteAnimation = rememberInfiniteTransition()
    val morphProgress = infiniteAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(
                durationMillis = wavesMorphAnimationDuration
            ),
            repeatMode = RepeatMode.Reverse
        )
    )
    val rotateProgress by infiniteAnimation.animateFloat(
        initialValue = 0f,
        targetValue = if (info.clockWiseRotation) {
            fullCircleDegrees
        } else {
            -fullCircleDegrees
        },
        animationSpec = infiniteRepeatable(
            tween(
                durationMillis = wavesRotationAnimationDuration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )
    val scaleProgress by infiniteAnimation.animateFloat(
        initialValue = info.scaleRange.start,
        targetValue = info.scaleRange.endInclusive,
        animationSpec = infiniteRepeatable(
            tween(
                durationMillis = wavesScaleAnimationDuration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    val alpha by infiniteAnimation.animateFloat(
        initialValue = info.alphaRange.start,
        targetValue = info.alphaRange.endInclusive,
        animationSpec = infiniteRepeatable(
            tween(
                durationMillis = wavesAlphaAnimationDuration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    val color = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .drawWithCache {
                val path = getPathMorph().toPath(progress = morphProgress.value)
                    .asComposePath()

                onDrawBehind {
                    scale(
                        scaleX = scaleProgress,
                        scaleY = scaleProgress
                    ) {
                        rotate(
                            degrees = rotateProgress
                        ) {
                            drawPath(
                                path = path,
                                color = color.copy(alpha = alpha),
                                style = Stroke(width = wavesShapeStrokeWidth)
                            )
                        }
                    }
                }
            }
            .fillMaxSize()
    )
}

private fun CacheDrawScope.getPathMorph(): Morph {
    val startShape = RoundedPolygon.star(
        numVerticesPerRadius = wavesStartShapeVerticesCount,
        radius = size.maxDimension / wavesShapeRadiusMultiplier,
        innerRadius = size.maxDimension / wavesShapeInnerRadiusMultiplier,
        rounding = CornerRounding(
            radius = size.maxDimension / wavesShapeCornerRadiusMultiplier,
            smoothing = wavesShapeSmoothing
        ),
        centerX = size.width / 2,
        centerY = size.height / 2
    )
    val endShape = RoundedPolygon.star(
        numVerticesPerRadius = wavesEndShapeVerticesCount,
        radius = size.maxDimension / wavesShapeRadiusMultiplier,
        innerRadius = size.maxDimension / wavesShapeInnerRadiusMultiplier,
        rounding = CornerRounding(
            radius = size.maxDimension / wavesShapeCornerRadiusMultiplier,
            smoothing = wavesShapeSmoothing
        ),
        centerX = size.width / 2,
        centerY = size.height / 2
    )
    return Morph(start = startShape, end = endShape)
}
