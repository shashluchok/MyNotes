package com.shashluchok.medianotes.presentation.screen.medianotes.data

import com.shashluchok.medianotes.presentation.components.snackbar.SnackbarData
import com.shashluchok.medianotes.presentation.data.AlertDialogData
import com.shashluchok.medianotes.presentation.data.NotificationData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf

internal data class MediaNotesState(
    val topBarTitle: String,
    val toolbarText: String = "",
    val editingMediaNote: MediaNoteItem.EditableMediaNoteItem? = null,
    val notes: ImmutableList<MediaNoteItem> = persistentListOf(),
    // Nested states
    val selectionState: SelectionState? = null,
    val recordingState: RecordingState? = null,
    // Notifications
    val tooltipVisible: Boolean = false,
    val alertDialogData: AlertDialogData? = null,
    val notificationData: NotificationData? = null,
    val snackbarData: SnackbarData? = null
) {
    data class RecordingState(
        val timerMillis: Long = 0L,
        val volumeLevel: Float = 0F
    )

    data class SelectionState(
        val notes: ImmutableList<MediaNoteItem> = persistentListOf(),
        val options: ImmutableSet<SelectionOption>
    ) {
        enum class SelectionOption {
            DELETE, COPY, EDIT
        }
    }
}
