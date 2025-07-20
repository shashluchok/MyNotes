package com.shashluchok.medianotes.presentation.screen.medianotes.mediatoolbar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.shashluchok.medianotes.presentation.components.mediaicon.MediaIconButton
import com.shashluchok.medianotes.presentation.components.mediaicon.MediaIconButtonDefaults
import com.shashluchok.medianotes.presentation.components.waves.WavedBox
import com.shashluchok.medianotes.presentation.screen.medianotes.data.MediaNotesState

private const val voiceRecordingIconTintAnimationDuration = 100
private const val voiceRecordingIconTintAnimationDelay = 100

@Composable
internal fun VoiceToolbarIcon(
    onClick: () -> Unit,
    recordingState: MediaNotesState.RecordingState?,
    modifier: Modifier = Modifier
) {
    val iconTintColor by animateColorAsState(
        targetValue = if (recordingState != null) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(
            durationMillis = voiceRecordingIconTintAnimationDuration,
            delayMillis = voiceRecordingIconTintAnimationDelay
        )
    )

    WavedBox(
        modifier = modifier,
        visible = recordingState != null,
        volumeLevel = recordingState?.volumeLevel ?: 0f
    ) {
        MediaIconButton(
            painter = rememberVectorPainter(Icons.Outlined.Mic),
            colors = MediaIconButtonDefaults.iconButtonColors(
                contentColor = iconTintColor
            ),
            onClick = onClick
        )
    }
}
