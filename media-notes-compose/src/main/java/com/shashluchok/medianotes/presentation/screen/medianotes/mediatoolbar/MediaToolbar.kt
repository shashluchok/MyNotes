package com.shashluchok.medianotes.presentation.screen.medianotes.mediatoolbar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.shashluchok.medianotes.R
import com.shashluchok.medianotes.presentation.RECORD_AUDIO
import com.shashluchok.medianotes.presentation.components.mediaicon.MediaIconButton
import com.shashluchok.medianotes.presentation.components.mediaicon.MediaIconButtonDefaults
import com.shashluchok.medianotes.presentation.components.tooltip.TooltipContainer
import com.shashluchok.medianotes.presentation.modifiers.pointerinput.draggableOnLongClick
import com.shashluchok.medianotes.presentation.screen.medianotes.data.MediaNotesState
import com.shashluchok.medianotes.presentation.utils.toPx
import kotlin.math.absoluteValue

private val toolbarMinHeight = 48.dp
private val toolbarHorizontalPadding = 4.dp
private val maxVoiceIconOffset = 120.dp
private val maxSwipeToCancelOffset = 30.dp
private const val swipeToCancelDragOffsetMultiplier = 0.25f
private val swipeToCancelAnimationBreakpoint = 10.dp

private val toolbarEditingHeaderPadding = PaddingValues(
    start = 16.dp,
    end = 4.dp
)
private const val toolbarEditingHeaderBackgroundAlpha = 0.12f
private val toolbarEditingHeaderHeight = 48.dp
private val toolbarEditingHeaderDividerThickness = 1.dp

private val textFieldVerticalPadding = 10.dp
private const val textFieldMaxLines = 5

private val voiceRecordingLayoutHorizontalPadding = 12.dp

private const val voiceRecordingLayoutEnterAnimationDuration = 250
private const val voiceRecordingLayoutExitAnimationDuration = 100

private const val swipeToCancelSlideAnimationDuration = 750
private const val swipeToCancelSlideMaxValue = 20f

private const val voiceIconTransitionAnimationDuration = 300

private val toolTipCloseIconSize = 24.dp
private val tooltipContentPadding = PaddingValues(
    start = 16.dp,
    top = 8.dp,
    bottom = 8.dp
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
internal fun MediaToolbar(
    text: String,
    editing: Boolean,
    tooltipVisible: Boolean,
    recordingState: MediaNotesState.RecordingState?,
    onCameraClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onVoiceLongClick: () -> Unit,
    onVoiceDragEnd: () -> Unit,
    onVoiceDragCancel: () -> Unit,
    onSketchClick: () -> Unit,
    onSendClick: () -> Unit,
    onTextChange: (String) -> Unit,
    onToolTipDismissRequest: () -> Unit,
    onRecordAudioPermissionDenied: () -> Unit,
    onRecordAudioPermissionUnavailable: () -> Unit,
    onCancelEditing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var voiceIconOffsetX by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(recordingState) {
        if (recordingState == null) {
            animate(
                initialValue = voiceIconOffsetX,
                targetValue = 0f,
                animationSpec = tween(durationMillis = voiceIconTransitionAnimationDuration)
            ) { value, _ -> voiceIconOffsetX = value }
        }
    }

    LaunchedEffect(editing) {
        if (editing) {
            keyboardController?.show()
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier.then(
            if (recordingState == null) {
                Modifier.animateContentSize(alignment = Alignment.BottomCenter)
            } else {
                Modifier
            }
        )
    ) {
        AnimatedVisibility(
            visible = editing,
            enter = fadeIn(tween()),
            exit = fadeOut(tween())
        ) {
            EditingHeader(
                modifier = Modifier.fillMaxWidth(),
                onCancelEditing = onCancelEditing
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = toolbarHorizontalPadding)
                .background(MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                CreateMediaNoteLayout(
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth(),
                    text = text,
                    onTextChange = onTextChange,
                    onCameraClick = onCameraClick,
                    onSendClick = onSendClick,
                    onSketchClick = onSketchClick,
                    editing = editing
                )
                this@Row.AnimatedVisibility(
                    visible = recordingState != null,
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
                        timerValueMillis = recordingState?.timerMillis ?: 0L,
                        swipeToCancelOffset = voiceIconOffsetX * swipeToCancelDragOffsetMultiplier,
                        isRecording = recordingState != null
                    )
                }
            }

            val recordAudioPermission = rememberPermissionState(
                RECORD_AUDIO
            )
            val requestPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (!granted) {
                    onRecordAudioPermissionDenied()
                }
            }

            AnimatedVisibility(
                visible = text.isEmpty() && !editing,
                enter = slideInHorizontally(spring(stiffness = Spring.StiffnessLow)) { it }
            ) {
                TooltipContainer(
                    toolTipVisible = tooltipVisible,
                    onToolTipDismiss = onToolTipDismissRequest,
                    animate = true,
                    tooltip = {
                        VoiceRecordingTooltipContent(
                            onCloseIconClick = onToolTipDismissRequest,
                            text = context.getString(R.string.screen_media_notes__tooltip__voice_recording__title)
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    val voiceRecordingEnabled =
                        recordAudioPermission.status == PermissionStatus.Granted
                    val haptic = LocalHapticFeedback.current

                    VoiceToolbarIcon(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = voiceIconOffsetX.toInt(),
                                    y = 0
                                )
                            }
                            .draggableOnLongClick(
                                onDrag = { offset ->
                                    // Drag only to the left
                                    if (offset < 0) {
                                        voiceIconOffsetX = offset
                                    }
                                },
                                onDragStart = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onVoiceLongClick()
                                },
                                onDragEnd = onVoiceDragEnd,
                                onDragCancel = onVoiceDragCancel,
                                maxDragOffset = maxVoiceIconOffset.toPx(),
                                enabled = voiceRecordingEnabled
                            ),
                        recordingState = recordingState,
                        onClick = {
                            if (voiceRecordingEnabled) {
                                onVoiceClick()
                            } else {
                                if (recordAudioPermission.status.shouldShowRationale) {
                                    onRecordAudioPermissionUnavailable()
                                } else {
                                    requestPermissionLauncher.launch(
                                        RECORD_AUDIO
                                    )
                                }
                            }
                        }

                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceRecordingTooltipContent(
    text: String,
    onCloseIconClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(tooltipContentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
        IconButton(
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
private fun EditingHeader(
    onCancelEditing: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary.copy(alpha = toolbarEditingHeaderBackgroundAlpha))
                .padding(toolbarEditingHeaderPadding)
                .height(toolbarEditingHeaderHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.screen_media_notes__toolbar__editing__title),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            MediaIconButton(
                painter = rememberVectorPainter(Icons.Rounded.Close),
                colors = MediaIconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.outlineVariant
                ),
                onClick = onCancelEditing
            )
        }
        HorizontalDivider(
            thickness = toolbarEditingHeaderDividerThickness,
            color = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun CreateMediaNoteLayout(
    text: String,
    editing: Boolean,
    onTextChange: (String) -> Unit,
    onSketchClick: () -> Unit,
    onCameraClick: () -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        MediaIconButton(
            painter = rememberVectorPainter(Icons.Outlined.Draw),
            onClick = onSketchClick
        )
        BasicTextField(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = textFieldVerticalPadding)
                .animateContentSize(),
            value = TextFieldValue(
                text = text,
                selection = TextRange(text.length)
            ),

            maxLines = textFieldMaxLines,
            onValueChange = {
                onTextChange(it.text)
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
                        text = stringResource(id = R.string.screen_media_notes__toolbar__hint),
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

        if (text.isEmpty() && !editing) {
            MediaIconButton(
                painter = rememberVectorPainter(Icons.Outlined.PhotoCamera),
                onClick = onCameraClick
            )
        } else {
            MediaIconButton(
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
                    .offset {
                        IntOffset(
                            x = ((innerAnimatedOffset + swipeToCancelOffset).toInt()),
                            y = 0
                        )
                    }
                    .alpha(alpha)
            )
        }
    }
}
