package com.shashluchok.medianotes.presentation.screen.medianotes.data

import android.content.Context
import androidx.compose.ui.platform.ClipboardManager

internal sealed interface MediaNotesAction {
    // Permissions
    data object OnCameraPermissionDenied : MediaNotesAction
    data object OnRecordAudioPermissionDenied : MediaNotesAction
    data class OnRequestPermissionUnavailable(val context: Context) : MediaNotesAction

    // Navigation
    data object OnNavigationIconClick : MediaNotesAction

    // Selection
    data class OnSelectMediaNote(val mediaNoteItem: MediaNoteItem) : MediaNotesAction
    data object OnCancelSelecting : MediaNotesAction
    data object OnDeleteMediaNotesClick : MediaNotesAction
    data class OnCopyMediaNoteClick(val clipboardManager: ClipboardManager) : MediaNotesAction

    // Editing
    data object OnEditMediaNoteClick : MediaNotesAction
    data object OnCancelEditClick : MediaNotesAction

    // Text note creation
    data class OnTextChange(val text: String) : MediaNotesAction
    data object OnSendClick : MediaNotesAction

    // Voice note creation
    data object OnVoiceClick : MediaNotesAction
    data class OnVoiceLongClick(val context: Context) : MediaNotesAction
    data object OnVoiceDragEnd : MediaNotesAction
    data object OnVoiceDragCancel : MediaNotesAction

    // Notifications
    data object OnNotificationDismiss : MediaNotesAction
    data object OnToolTipDismissRequest : MediaNotesAction
}
