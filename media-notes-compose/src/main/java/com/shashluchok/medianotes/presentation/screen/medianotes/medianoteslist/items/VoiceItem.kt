package com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist.items

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.shashluchok.medianotes.R
import com.shashluchok.medianotes.presentation.components.waves.WavedBox
import com.shashluchok.medianotes.presentation.screen.medianotes.MediaNoteItem
import kotlinx.collections.immutable.ImmutableList

private val voicePadding = 12.dp
private val containerVerticalArrangement = 12.dp

private val playIconContainerSize = 40.dp
private val playIconSize = 20.dp

private const val playIconWavesScaleMultiplier = 1.35f
private const val playIconCircleScaleMultiplier = 1f

private val visualizerHeight = 24.dp
private val visualizerItemsSpacing = 1.dp

@Composable
internal fun VoiceItem(
    voice: MediaNoteItem.Voice,
    duration: String,
    isPlaying: Boolean,
    playProgress: Float,
    seekEnabled: Boolean,
    onSeekStart: (MediaNoteItem.Voice, progress: Float) -> Unit,
    onSeek: (MediaNoteItem.Voice, progress: Float) -> Unit,
    onSeekEnd: (MediaNoteItem.Voice) -> Unit,
    onPlay: (MediaNoteItem.Voice) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(voicePadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(containerVerticalArrangement)
    ) {
        val progress = remember { Animatable(0f) }

        LaunchedEffect(isPlaying) {
            if (isPlaying.not()) {
                progress.animateTo(0f, animationSpec = tween())
            } else {
                progress.animateTo(1f, animationSpec = tween())
            }
        }

        val composition by rememberLottieComposition(
            if (isSystemInDarkTheme()) {
                LottieCompositionSpec.RawRes(R.raw.lottie_player_dark)
            } else {
                LottieCompositionSpec.RawRes(R.raw.lottie_player)
            }
        )

        WavedBox(
            visible = isPlaying,
            volumeLevel = getPeakByProgress(voice.peaks, playProgress),
            wavesSizeMultiplier = playIconWavesScaleMultiplier,
            circleSizeMultiplier = playIconCircleScaleMultiplier
        ) {
            Box(
                modifier = Modifier
                    .size(playIconContainerSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(
                        onClick = {
                            onPlay(voice)
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    modifier = Modifier.size(playIconSize),
                    composition = composition,
                    progress = { progress.value }
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            VolumeVisualizer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(visualizerHeight),
                peaks = voice.peaks,
                progress = playProgress,
                onSeek = {
                    onSeek(voice, it)
                },
                onSeekStart = {
                    onSeekStart(voice, it)
                },
                onSeekEnd = {
                    onSeekEnd(voice)
                },
                seekEnabled = seekEnabled
            )

            Text(
                text = duration,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VolumeVisualizer(
    peaks: ImmutableList<Float>,
    progress: Float,
    onSeekStart: (progress: Float) -> Unit,
    onSeek: (progress: Float) -> Unit,
    onSeekEnd: () -> Unit,
    seekEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.primaryContainer

    Canvas(
        modifier = modifier.then(
            if (seekEnabled) {
                Modifier.pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val seekProgress = offset.x / size.width
                            onSeekStart(seekProgress.coerceIn(0f, 1f))
                        },
                        onDrag = { change, _ ->
                            val seekProgress = change.position.x / size.width
                            onSeek(seekProgress.coerceIn(0f, 1f))
                        },
                        onDragEnd = onSeekEnd
                    )
                }.pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            val seekProgress = offset.x / size.width
                            onSeekStart(seekProgress.coerceIn(0f, 1f))
                        },
                        onTap = {
                            onSeekEnd()
                        }
                    )
                }
            } else {
                Modifier
            }
        )
    ) {
        val spacingPx = visualizerItemsSpacing.toPx()
        val availableWidth = size.width
        val availableHeight = size.height

        val totalSpacing = spacingPx * (peaks.size - 1)
        val lineWidthPx = ((availableWidth - totalSpacing) / peaks.size).coerceAtLeast(1f)

        drawIntoCanvas { canvas ->
            val paint = Paint()
            canvas.saveLayer(Rect(0f, 0f, size.width, size.height), paint)
            peaks.onEachIndexed { index, peak ->
                val x = index * (lineWidthPx + spacingPx)
                val height = availableHeight * peak
                drawRoundRect(
                    color = inactiveColor,
                    topLeft = Offset(
                        x = x,
                        y = (availableHeight / 2) - height / 2
                    ),
                    size = Size(width = lineWidthPx, height = height),
                    cornerRadius = CornerRadius(x = lineWidthPx / 2, y = lineWidthPx / 2)
                )
            }
            canvas.drawRect(
                Rect(0f, 0f, availableWidth * progress, availableHeight),
                Paint().apply {
                    color = activeColor
                    blendMode = BlendMode.SrcIn
                }
            )

            canvas.restore()
        }
    }
}

private fun getPeakByProgress(peaks: ImmutableList<Float>, progress: Float): Float {
    val index = (peaks.lastIndex * progress).toInt().coerceAtMost(peaks.lastIndex)
    return peaks[index]
}
