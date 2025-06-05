package com.shashluchok.medianotes.presentation.screen.medianotes

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.shashluchok.audiorecorder.audio.FileDataSource
import com.shashluchok.audiorecorder.audio.codec.mpg123.Mpg123Decoder
import com.shashluchok.medianotes.R
import com.shashluchok.medianotes.container.AppInfoProvider
import com.shashluchok.medianotes.data.MediaNote
import com.shashluchok.medianotes.domain.notes.delete.DeleteMediaNotesInteractor
import com.shashluchok.medianotes.domain.notes.get.GetMediaNotesInteractor
import com.shashluchok.medianotes.domain.settings.OpenSettingsInteractor
import com.shashluchok.medianotes.presentation.components.snackbar.SnackbarData
import com.shashluchok.medianotes.presentation.screen.AbsViewModel
import com.shashluchok.medianotes.presentation.utils.addOrRemove
import com.shashluchok.medianotes.presentation.utils.copyModified
import com.shashluchok.medianotes.presentation.utils.toAudioDisplayString
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import java.io.File

internal class MediaNotesViewModel(
    private val getMediaNotesInteractor: GetMediaNotesInteractor,
    private val deleteMediaNote: DeleteMediaNotesInteractor,
    private val appInfoProvider: AppInfoProvider,
    private val openSettings: OpenSettingsInteractor
) : AbsViewModel<MediaNotesViewModel.State>() {

    data class Selection(
        val notes: ImmutableList<MediaNoteItem> = persistentListOf(),
        val options: ImmutableSet<SelectionOption>
    )

    enum class SelectionOption {
        DELETE
    }

    data class State(
        val topBarTitle: String,
        val selection: Selection? = null,
        val snackbarData: SnackbarData? = null,
        val editingMediaNote: MediaNoteItem.EditableMediaNoteItem? = null,
        val notes: ImmutableList<MediaNoteItem> = persistentListOf()
    )

    sealed interface Action {
        data object OnCameraPermissionDenied : Action
        data object OnRecordAudioPermissionDenied : Action
        data class OnRequestPermissionUnavailable(val context: Context) : Action
        data object OnNavigationIconClick : Action
        data class OnSelectionOptionClick(val selectionOption: SelectionOption) : Action
        data class OnSelectMediaNote(val mediaNoteItem: MediaNoteItem) : Action
        data object OnCancelSelecting : Action
    }

    override val mutableStateFlow: MutableStateFlow<State> = MutableStateFlow(
        State(
            topBarTitle = appInfoProvider.topBarConfiguration.title
        )
    )

    private val decoder = Mpg123Decoder()

    init {
        subscribeToMediaNotes()
    }

    override fun onCleared() {
        decoder.close()
        super.onCleared()
    }

    fun onAction(action: Action) {
        when (action) {
            Action.OnCameraPermissionDenied -> showSnackbar(
                snackbarData = SnackbarData(
                    titleResId = R.string.screen_media_notes__snackbar__permissions_denied__title,
                    onDismiss = ::onDismissSnackbar
                )
            )

            Action.OnRecordAudioPermissionDenied -> showSnackbar(
                snackbarData = SnackbarData(
                    titleResId = R.string.screen_media_notes__snackbar__permissions_denied__title,
                    onDismiss = ::onDismissSnackbar
                )
            )

            is Action.OnRequestPermissionUnavailable -> showSnackbar(
                snackbarData = SnackbarData(
                    titleResId = R.string.screen_media_notes__snackbar__go_to_settings__title,
                    actionTitleResId = R.string.screen_media_notes__snackbar__go_to_settings__action,
                    action = { showSettings(action.context) },
                    onDismiss = ::onDismissSnackbar
                )
            )

            Action.OnNavigationIconClick -> onNavigationIconClick()
            is Action.OnSelectionOptionClick -> onSelectionOptionClick(action.selectionOption)
            is Action.OnSelectMediaNote -> onSelect(action.mediaNoteItem)
            Action.OnCancelSelecting -> cancelSelecting()
        }
    }

    private fun subscribeToMediaNotes() {
        viewModelScope.launch {
            getMediaNotesInteractor.mediaNotesFlow.collect { notes ->
                mutableStateFlow.update {
                    it.copy(
                        notes = notes.map { note ->
                            note.toMediaNoteItem()
                        }.toImmutableList()
                    )
                }
            }
        }
    }

    private fun onNavigationIconClick() {
        if (state.selection != null) {
            cancelSelecting()
        } else {
            appInfoProvider.topBarConfiguration.onDismiss()
        }
    }

    private fun onSelect(noteItem: MediaNoteItem) {
        val selectedNotes = (state.selection?.notes ?: persistentListOf()).copyModified {
            addOrRemove(noteItem)
        }

        val selection = if (selectedNotes.isNotEmpty()) {
            Selection(
                notes = selectedNotes,
                options = selectedNotes.toSelectionOptions()
            )
        } else {
            null
        }

        mutableStateFlow.update {
            it.copy(
                selection = selection,
                topBarTitle = selection?.let {
                    selection.notes.size.toString()
                } ?: appInfoProvider.topBarConfiguration.title

            )
        }
    }

    private fun onSelectionOptionClick(selectionOption: SelectionOption) {
        val selectedNotes = state.selection?.notes ?: return
        when (selectionOption) {
            /*SelectionOption.EDIT -> {
                if (selectedNotes.size != 1) return
                mutableStateFlow.update {
                    it.copy(editingMediaNote = (selectedNotes.first() as? MediaNoteItem.EditableMediaNoteItem))
                }
            }*/

            SelectionOption.DELETE -> {
                viewModelScope.launch {
                    deleteMediaNote(
                        *selectedNotes.map { it.id }.toTypedArray()
                    )
                }
            }
        }
        cancelSelecting()
    }

    private fun cancelSelecting() {
        mutableStateFlow.update {
            it.copy(
                selection = null,
                topBarTitle = appInfoProvider.topBarConfiguration.title
            )
        }
    }

    private fun showSnackbar(snackbarData: SnackbarData) {
        mutableStateFlow.update {
            it.copy(snackbarData = snackbarData)
        }
    }

    private fun onDismissSnackbar() {
        state = state.copy(
            snackbarData = null
        )
    }

    private fun showSettings(context: Context) = openSettings(context)

    private fun ImmutableList<MediaNoteItem>.toSelectionOptions(): ImmutableSet<SelectionOption> {
        val editable = size == 1 && any { it is MediaNoteItem.Voice }.not()
        val copyable = size == 1 && first() is MediaNoteItem.Text
        return buildList {
            add(SelectionOption.DELETE)
            /*if (editable) add(SelectionOption.EDIT)
            if (copyable) add(SelectionOption.COPY)*/
        }.toImmutableSet()
    }

    private suspend fun MediaNote.toMediaNoteItem() =
        when (this) {
            is MediaNote.Image -> MediaNoteItem.Image(
                id = id,
                updatedAt = updatedAt.convertToDateString(),
                path = path,
                text = text
            )

            is MediaNote.Sketch -> MediaNoteItem.Sketch(
                id = id,
                updatedAt = updatedAt.convertToDateString(),
                path = path
            )

            is MediaNote.Text -> MediaNoteItem.Text(
                id = id,
                updatedAt = updatedAt.convertToDateString(),
                value = value
            )

            is MediaNote.Voice -> {
                val (peaks, duration) = decoder.calculateVolumeLevelsAndDuration(
                    FileDataSource(file = File(path))
                )
                MediaNoteItem.Voice(
                    id = id,
                    updatedAt = updatedAt.convertToDateString(),
                    path = path,
                    peaks = peaks,
                    duration = duration.toAudioDisplayString(),
                    durationMillis = duration.inWholeMilliseconds
                )
            }
        }

    private fun Instant.convertToDateString(): String {
        val dateFormat = LocalDateTime.Format {
            hour(); char(':'); minute()
            char(' '); char('|'); char(' ')
            dayOfMonth(); char('.'); monthNumber(); char('.'); year()
        }
        return this.toLocalDateTime(TimeZone.currentSystemDefault()).format(dateFormat)
    }
}
