package com.shashluchok.medianotes.presentation.components.mediatoolbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.shashluchok.medianotes.R
import com.shashluchok.medianotes.presentation.components.tooltip.TooltipContainer
import com.shashluchok.medianotes.presentation.modifiers.drag.draggableOnLongClick
import com.shashluchok.medianotes.presentation.modifiers.shadow.ShadowPosition
import com.shashluchok.medianotes.presentation.modifiers.shadow.shadow
import com.shashluchok.medianotes.presentation.toPx
import kotlinx.collections.immutable.persistentSetOf
import kotlin.math.absoluteValue

// General
private val toolbarDividerThickness = 1.dp
private val toolbarMinHeight = 48.dp
private val toolbarHorizontalPadding = 4.dp
private val maxVoiceIconOffset = 120.dp
private val maxSwipeToCancelOffset = 30.dp
private const val swipeToCancelDragOffsetMultiplier = 0.25f
private val swipeToCancelAnimationBreakpoint = 10.dp

// TextFieldLayout
private val textFieldVerticalPadding = 12.dp

// VoiceRecordingLayout
private val voiceRecordingLayoutHorizontalPadding = 12.dp

private const val voiceRecordingLayoutEnterAnimationDuration = 250
private const val voiceRecordingLayoutExitAnimationDuration = 100

private const val swipeToCancelSlideAnimationDuration = 750
private const val swipeToCancelSlideMaxValue = 20f

private const val voiceIconTransitionAnimationDuration = 300

// Tooltip
private val toolTipCloseIconSize = 24.dp
private val tooltipContentPadding = PaddingValues(
    start = 16.dp,
    top = 8.dp,
    bottom = 8.dp
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MediaToolbar(
    text: String,
    isRecording: Boolean,
    timerValueMillis: Long,
    onCameraClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onVoiceLongClick: () -> Unit,
    onVoiceLongDragEnd: () -> Unit,
    onVoiceDragCancel: () -> Unit,
    onSketchClick: () -> Unit,
    onSendClick: () -> Unit,
    onTextChange: (String) -> Unit,
    toolTipVisible: Boolean,
    onToolTipDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    shadowVisible: Boolean = true
) {
    var voiceIconOffsetX by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isRecording) {
        if (isRecording.not()) {
            animate(
                initialValue = voiceIconOffsetX,
                targetValue = 0f,
                animationSpec = tween(durationMillis = voiceIconTransitionAnimationDuration)
            ) { value, _ -> voiceIconOffsetX = value }
        }
    }

    Column(
        modifier = modifier
            .testTag(tag = MediaToolbar.Tag.root)
            .windowInsetsPadding(WindowInsets.safeDrawing.exclude(WindowInsets.statusBars))
            .shadow(
                shadowPositions = persistentSetOf(ShadowPosition.TOP),
                shadowVisible = shadowVisible
            )
    ) {
        HorizontalDivider(thickness = toolbarDividerThickness)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = toolbarHorizontalPadding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                CreateMediaNoteLayout(
                    modifier = Modifier
                        .heightIn(min = toolbarMinHeight)
                        .fillMaxWidth(),
                    text = text,
                    onTextChange = onTextChange,
                    onCameraClick = onCameraClick,
                    onSendClick = onSendClick,
                    onSketchClick = onSketchClick
                )
                this@Row.AnimatedVisibility(
                    visible = isRecording,
                    enter = slideInVertically(
                        animationSpec = tween(voiceRecordingLayoutEnterAnimationDuration),
                        initialOffsetY = { it }
                    ) + fadeIn(
                        animationSpec = tween(voiceRecordingLayoutEnterAnimationDuration)
                    ),
                    exit = fadeOut(
                        animationSpec = tween(voiceRecordingLayoutExitAnimationDuration)
                    )
                ) {
                    RecordVoiceToolbarLayout(
                        modifier = Modifier
                            .heightIn(min = toolbarMinHeight)
                            .fillMaxWidth(),
                        timerValueMillis = timerValueMillis,
                        swipeToCancelOffset = voiceIconOffsetX * swipeToCancelDragOffsetMultiplier,
                        isRecording = isRecording
                    )
                }
            }

            AnimatedVisibility(
                visible = text.isEmpty(),
                enter = slideInHorizontally(spring(stiffness = Spring.StiffnessLow)) { it }
            ) {
                TooltipContainer(
                    toolTipVisible = toolTipVisible,
                    onToolTipDismiss = onToolTipDismissRequest,
                    animate = true,
                    tooltip = { VoiceRecordingTooltip(onCloseIconClick = onToolTipDismissRequest) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    VoiceToolbarIcon(
                        modifier = Modifier
                            .testTag(tag = MediaToolbar.Tag.voiceIcon)
                            .offset { IntOffset(x = voiceIconOffsetX.toInt(), y = 0) }
                            .draggableOnLongClick(
                                onDrag = { offset ->
                                    // Drag only to the left
                                    if (offset < 0) {
                                        voiceIconOffsetX = offset
                                    }
                                },
                                onDragStart = { onVoiceLongClick() },
                                onDragEnd = { onVoiceLongDragEnd() },
                                onDragCancel = { onVoiceDragCancel() },
                                maxDragOffset = maxVoiceIconOffset.toPx()
                            ),
                        isRecording = isRecording,
                        onClick = onVoiceClick
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceRecordingTooltip(
    modifier: Modifier = Modifier,
    onCloseIconClick: () -> Unit
) {
    Row(
        modifier = modifier.padding(tooltipContentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.testTag(tag = MediaToolbar.Tag.toolTipText),
            text = stringResource(
                R.string.media_notes_toolbar__voice_recording__tooltip__title
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
        IconButton(
            modifier = Modifier.testTag(tag = MediaToolbar.Tag.toolTipIcon),
            onClick = onCloseIconClick
        ) {
            Icon(
                modifier = Modifier.size(toolTipCloseIconSize),
                painter = rememberVectorPainter(Icons.Rounded.Cancel),
                tint = MaterialTheme.colorScheme.onPrimary,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun CreateMediaNoteLayout(
    text: String,
    onTextChange: (String) -> Unit,
    onSketchClick: () -> Unit,
    onCameraClick: () -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .testTag(tag = MediaToolbar.Tag.createMediaNoteLayout)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        ToolbarIcon(
            modifier = Modifier.testTag(tag = MediaToolbar.Tag.sketchIcon),
            painter = rememberVectorPainter(Icons.Outlined.Draw),
            onClick = onSketchClick
        )
        BasicTextField(
            modifier = Modifier
                .testTag(tag = MediaToolbar.Tag.textInput)
                .weight(1f)
                .padding(vertical = textFieldVerticalPadding)
                .animateContentSize(),
            value = text,
            onValueChange = {
                onTextChange(it.trim())
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    onSendClick()
                }
            ),
            decorationBox = { innerTextField ->
                if (text.isEmpty()) {
                    Text(
                        modifier = Modifier.testTag(tag = MediaToolbar.Tag.textHint),
                        text = stringResource(id = R.string.media_notes_toolbar__text_field__hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                innerTextField()
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
        )

        if (text.isEmpty()) {
            ToolbarIcon(
                modifier = Modifier.testTag(tag = MediaToolbar.Tag.cameraIcon),
                painter = rememberVectorPainter(Icons.Outlined.PhotoCamera),
                onClick = onCameraClick
            )
        } else {
            ToolbarIcon(
                modifier = Modifier.testTag(tag = MediaToolbar.Tag.sendIcon),
                painter = rememberVectorPainter(Icons.AutoMirrored.Filled.Send),
                onClick = onSendClick
            )
        }
    }
}

@Composable
private fun RecordVoiceToolbarLayout(
    isRecording: Boolean,
    swipeToCancelOffset: Float,
    timerValueMillis: Long,
    modifier: Modifier = Modifier
) {
    var innerAnimatedOffset by remember {
        mutableFloatStateOf(0f)
    }

    val currentOffset = swipeToCancelOffset.absoluteValue
    val animateSwipeToCancel = currentOffset < swipeToCancelAnimationBreakpoint.toPx()

    LaunchedEffect(animateSwipeToCancel) {
        if (animateSwipeToCancel) {
            // Animate to initial position before infinite animation starts again
            if (innerAnimatedOffset != 0f) {
                animate(
                    initialValue = innerAnimatedOffset,
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = swipeToCancelSlideAnimationDuration)
                ) { value, _ -> innerAnimatedOffset = value }
            }
            animate(
                initialValue = 0f,
                targetValue = -swipeToCancelSlideMaxValue,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = swipeToCancelSlideAnimationDuration,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            ) { value, _ -> innerAnimatedOffset = value }
        }
    }

    Row(
        modifier = modifier
            .testTag(tag = MediaToolbar.Tag.recordVoiceToolbarLayout)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = voiceRecordingLayoutHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StopWatch(timerValueMillis = timerValueMillis)

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val maxOffset = maxSwipeToCancelOffset.toPx()
            val alpha = if (isRecording) {
                (maxOffset - currentOffset) / maxOffset
            } else {
                0f
            }

            SwipeToCancelItem(
                modifier = Modifier
                    .offset { IntOffset(x = ((innerAnimatedOffset + swipeToCancelOffset).toInt()), y = 0) }
                    .alpha(alpha)
            )
        }
    }
}

object MediaToolbar {
    object Tag {
        const val root = "MediaToolbar"
        const val recordVoiceToolbarLayout = "$root.RecordVoiceToolbarLayout"
        const val createMediaNoteLayout = "$root.CreateMediaNoteLayout"
        const val textInput = "$root.TextInput"
        const val textHint = "$root.TextHint"
        const val sketchIcon = "$root.SketchIcon"
        const val cameraIcon = "$root.CameraIcon"
        const val sendIcon = "$root.CameraIcon"
        const val voiceIcon = "$root.CameraIcon"
        const val toolTipText = "$root.TooltipText"
        const val toolTipIcon = "$root.ToolTipIcon"
    }
}
