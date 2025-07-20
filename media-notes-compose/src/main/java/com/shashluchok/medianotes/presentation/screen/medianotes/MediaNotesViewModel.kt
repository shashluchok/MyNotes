package com.shashluchok.medianotes.presentation.screen.medianotes

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.viewModelScope
import com.shashluchok.audiorecorder.audio.AudioRecorderImpl
import com.shashluchok.audiorecorder.audio.FileDataSource
import com.shashluchok.audiorecorder.audio.codec.mpg123.Mpg123Decoder
import com.shashluchok.medianotes.R
import com.shashluchok.medianotes.container.AppInfoProvider
import com.shashluchok.medianotes.data.MediaNote
import com.shashluchok.medianotes.domain.notes.create.CreateMediaNoteInteractor
import com.shashluchok.medianotes.domain.notes.delete.DeleteMediaNotesInteractor
import com.shashluchok.medianotes.domain.notes.get.GetMediaNotesInteractor
import com.shashluchok.medianotes.domain.notes.update.UpdateMediaNoteInteractor
import com.shashluchok.medianotes.domain.settings.OpenSettingsInteractor
import com.shashluchok.medianotes.presentation.components.snackbar.SnackbarData
import com.shashluchok.medianotes.presentation.data.ActionButton
import com.shashluchok.medianotes.presentation.data.AlertDialogData
import com.shashluchok.medianotes.presentation.data.NotificationData
import com.shashluchok.medianotes.presentation.screen.AbsViewModel
import com.shashluchok.medianotes.presentation.screen.medianotes.data.MediaNoteItem
import com.shashluchok.medianotes.presentation.screen.medianotes.data.MediaNotesAction
import com.shashluchok.medianotes.presentation.screen.medianotes.data.MediaNotesState
import com.shashluchok.medianotes.presentation.screen.medianotes.data.MediaNotesState.SelectionState.SelectionOption
import com.shashluchok.medianotes.presentation.screen.medianotes.data.toMediaNoteItem
import com.shashluchok.medianotes.presentation.utils.addOrRemove
import com.shashluchok.medianotes.presentation.utils.copyModified
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class MediaNotesViewModel(
    private val appInfoProvider: AppInfoProvider,
    private val openSettings: OpenSettingsInteractor,
    private val getMediaNotesInteractor: GetMediaNotesInteractor,
    private val deleteMediaNote: DeleteMediaNotesInteractor,
    private val createMediaNote: CreateMediaNoteInteractor,
    private val updateMediaNote: UpdateMediaNoteInteractor,
    private val resources: Resources
) : AbsViewModel<MediaNotesState>() {

    private val recorder = AudioRecorderImpl()
    private val decoder = Mpg123Decoder()

    private var timerJob: Job? = null
    private var voiceClicks = 0
    private var file: File? = null

    override val mutableStateFlow: MutableStateFlow<MediaNotesState> = MutableStateFlow(
        MediaNotesState(
            topBarTitle = appInfoProvider.topBarConfiguration.title
        )
    )

    init {
        subscribeToMediaNotes()
    }

    override fun onCleared() {
        timerJob?.cancel()
        decoder.close()
        recorder.destroy()
        super.onCleared()
    }

    fun onAction(action: MediaNotesAction) {
        when (action) {
            MediaNotesAction.OnCameraPermissionDenied -> showSnackbar(
                snackbarData = SnackbarData(
                    title = resources.getString(R.string.screen_media_notes__snackbar__permissions_denied__title),
                    onDismiss = ::onDismissSnackbar
                )
            )

            MediaNotesAction.OnRecordAudioPermissionDenied -> showSnackbar(
                snackbarData = SnackbarData(
                    title = resources.getString(R.string.screen_media_notes__snackbar__permissions_denied__title),
                    onDismiss = ::onDismissSnackbar
                )
            )

            is MediaNotesAction.OnRequestPermissionUnavailable -> showSnackbar(
                snackbarData = SnackbarData(
                    title = resources.getString(R.string.screen_media_notes__snackbar__go_to_settings__title),
                    actionTitle = resources.getString(R.string.screen_media_notes__snackbar__go_to_settings__action),
                    action = { showSettings(action.context) },
                    onDismiss = ::onDismissSnackbar
                )
            )

            MediaNotesAction.OnNavigationIconClick -> onNavigationIconClick()
            is MediaNotesAction.OnSelectMediaNote -> onSelect(action.mediaNoteItem)
            MediaNotesAction.OnCancelSelecting -> cancelSelecting()
            MediaNotesAction.OnNotificationDismiss -> onNotificationDismiss()
            is MediaNotesAction.OnCopyMediaNoteClick -> {
                val selectedNote = state.selectionState?.notes
                if (selectedNote?.size != 1) return

                (selectedNote.firstOrNull() as? MediaNoteItem.WithText)?.let {
                    action.clipboardManager.setText(AnnotatedString(it.text))
                    mutableStateFlow.update {
                        it.copy(
                            notificationData = NotificationData(
                                iconType = NotificationData.IconType.TEXT_COPIED,
                                message = R.string.screen_media_notes__notification__text_copied__title
                            )
                        )
                    }
                    cancelSelecting()
                }
            }

            MediaNotesAction.OnDeleteMediaNotesClick -> {
                val selectedNotes = state.selectionState?.notes ?: return
                showDeleteMediaNotesAlertDialog(selectedNotes)
            }

            MediaNotesAction.OnEditMediaNoteClick -> {
                val selectedNotes = state.selectionState?.notes
                if (selectedNotes?.size != 1) return
                cancelSelecting()
                (selectedNotes.first() as? MediaNoteItem.WithText)?.let { note ->
                    mutableStateFlow.update {
                        it.copy(
                            editingMediaNote = note,
                            toolbarText = note.text
                        )
                    }
                }
            }

            MediaNotesAction.OnCancelEditClick -> {
                mutableStateFlow.update {
                    it.copy(
                        editingMediaNote = null,
                        toolbarText = ""
                    )
                }
            }

            MediaNotesAction.OnSendClick -> onSendClick()
            is MediaNotesAction.OnTextChange -> onTextChange(action.text)

            MediaNotesAction.OnToolTipDismissRequest -> onDismissToolTip()
            MediaNotesAction.OnVoiceClick -> onVoiceClick()
            MediaNotesAction.OnVoiceDragCancel -> onStopRecording(save = false)
            MediaNotesAction.OnVoiceDragEnd -> onStopRecording(save = true)
            is MediaNotesAction.OnVoiceLongClick -> onStartRecording(context = action.context)
        }
    }

    private fun subscribeToMediaNotes() {
        viewModelScope.launch {
            getMediaNotesInteractor.mediaNotesFlow.collect { notes ->
                mutableStateFlow.update {
                    it.copy(
                        notes = notes
                            .sortedBy { it.createdAt }
                            .map {
                                it.toMediaNoteItem(decoder = decoder, resources = resources)
                            }
                            .toImmutableList()
                    )
                }
            }
        }
    }

    private fun onSendClick() {
        viewModelScope.launch {
            val editingTextNote = state.editingMediaNote

            editingTextNote?.let {
                if (state.toolbarText.isEmpty() && editingTextNote is MediaNoteItem.Text) {
                    showDeleteMediaNotesAlertDialog(persistentListOf(editingTextNote))
                } else {
                    val mediaNote = getMediaNotesInteractor.mediaNotesFlow.value.firstOrNull {
                        it.id == editingTextNote.id
                    } as? MediaNote.WithText ?: return@let null
                    updateMediaNote(
                        when (mediaNote) {
                            is MediaNote.Image -> mediaNote.copy(text = state.toolbarText)
                            is MediaNote.Text -> mediaNote.copy(text = state.toolbarText)
                        }
                    )
                }
            } ?: createMediaNote(
                MediaNote.Text(
                    text = state.toolbarText
                )
            )

            mutableStateFlow.update {
                it.copy(
                    toolbarText = "",
                    editingMediaNote = null
                )
            }
        }
    }

    private fun showDeleteMediaNotesAlertDialog(
        notesToDelete: ImmutableList<MediaNoteItem>
    ) {
        mutableStateFlow.update {
            it.copy(
                alertDialogData = AlertDialogData(
                    onDismiss = ::onDismissDialog,
                    title = if (notesToDelete.size == 1) {
                        resources.getString(R.string.screen_media_notes__dialog__note_delete__title)
                    } else {
                        resources.getString(R.string.screen_media_notes__dialog__notes_delete__title)
                    },
                    confirmButton = ActionButton(
                        title = resources.getString(
                            R.string.screen_media_notes__dialog__note_delete__confirm_button
                        ),
                        onClick = {
                            viewModelScope.launch {
                                deleteMediaNote(
                                    *notesToDelete.map { it.id }.toTypedArray()
                                )
                                cancelSelecting()
                                onDismissDialog()
                            }
                        }
                    ),
                    message = if (notesToDelete.size == 1) {
                        resources.getString(R.string.screen_media_notes__dialog__note_delete__message)
                    } else {
                        resources.getString(R.string.screen_media_notes__dialog__notes_delete__message)
                    },
                    dismissButton = ActionButton(
                        title = resources.getString(
                            R.string.screen_media_notes__dialog__note_delete__cancel_button
                        ),
                        onClick = ::onDismissDialog
                    )
                )
            )
        }
    }

    private fun onTextChange(text: String) {
        state = state.copy(
            toolbarText = text.trim()
        )
    }

    private fun onNavigationIconClick() {
        if (state.selectionState != null) {
            cancelSelecting()
        } else {
            appInfoProvider.topBarConfiguration.onDismiss()
        }
    }

    private fun onSelect(noteItem: MediaNoteItem) {
        val selectedNotes = (state.selectionState?.notes ?: persistentListOf()).copyModified {
            addOrRemove(noteItem)
        }

        val selectionState = if (selectedNotes.isNotEmpty()) {
            MediaNotesState.SelectionState(
                notes = selectedNotes,
                options = selectedNotes.toSelectionOptions()
            )
        } else {
            null
        }

        mutableStateFlow.update {
            it.copy(
                selectionState = selectionState
            )
        }
    }

    private fun onDismissDialog() {
        mutableStateFlow.update {
            it.copy(
                alertDialogData = null
            )
        }
    }

    private fun cancelSelecting() {
        mutableStateFlow.update {
            it.copy(
                selectionState = null,
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

    private fun onNotificationDismiss() {
        mutableStateFlow.update {
            it.copy(
                notificationData = null
            )
        }
    }

    private fun onStartRecording(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            file = createAudioFile(context)
            recorder.record(
                dataSource = FileDataSource(file ?: return@launch),
                onNewVolume = { newLevel ->
                    mutableStateFlow.update {
                        it.copy(
                            recordingState = it.recordingState?.copy(
                                volumeLevel = newLevel
                            )
                        )
                    }
                },
                onStart = {
                    voiceClicks = -1
                    timerJob?.cancel()
                    timerJob = launch {
                        val initialTime = System.currentTimeMillis()
                        mutableStateFlow.update {
                            it.copy(
                                recordingState = MediaNotesState.RecordingState()
                            )
                        }
                        while (isActive) {
                            mutableStateFlow.update {
                                it.copy(
                                    recordingState = it.recordingState?.copy(
                                        timerMillis = System.currentTimeMillis() - initialTime
                                    )
                                )
                            }
                            delay(TIMER_UPDATE_FREQUENCY)
                            if ((state.recordingState?.timerMillis ?: 0) > MAX_RECORDING_DURATION.inWholeMilliseconds) {
                                onStopRecording(save = true)
                                showSnackbar(
                                    SnackbarData(
                                        title = resources.getString(
                                            R.string.screen_media_notes__snackbar__max_audio_duration__title
                                        ),
                                        onDismiss = ::onDismissSnackbar
                                    )
                                )
                                cancel()
                            }
                        }
                    }
                }
            )
        }
    }

    private fun onStopRecording(save: Boolean) {
        viewModelScope.launch {
            recorder.stop()
            timerJob?.cancel()

            val recordingDuration =
                (state.recordingState?.timerMillis ?: 0L).toDuration(DurationUnit.MILLISECONDS)

            val isNotLongEnough = recordingDuration < MIN_RECORDING_DURATION

            file?.let {
                if (save.not() || isNotLongEnough) {
                    it.delete()
                } else {
                    createMediaNote(
                        MediaNote.Voice(path = it.path)
                    )
                }
            }
            file = null

            mutableStateFlow.update {
                it.copy(
                    recordingState = null
                )
            }
        }
    }

    private fun onVoiceClick() {
        if (voiceClicks % VOICE_TOOLTIP_CLICKS_COUNT == 0) {
            showRecordingToolTip()
        }
        voiceClicks++
    }

    private fun showRecordingToolTip() {
        state = state.copy(
            tooltipVisible = true
        )
    }

    private fun onDismissToolTip() {
        state = state.copy(
            tooltipVisible = false
        )
    }

    private fun createAudioFile(context: Context): File {
        return File(
            context.filesDir,
            "${Clock.System.now().epochSeconds}.$AUDIO_MP3_EXTENSION"
        )
    }

    private fun ImmutableList<MediaNoteItem>.toSelectionOptions(): ImmutableSet<SelectionOption> {
        val editable = size == 1 && any {
            it is MediaNoteItem.WithText
        }
        val copyable = size == 1 && any {
            it is MediaNoteItem.WithText &&
                it.text.isNotEmpty()
        }
        return buildList {
            if (copyable) add(SelectionOption.COPY)
            add(SelectionOption.DELETE)
            if (editable) add(SelectionOption.EDIT)
        }.toImmutableSet()
    }

    companion object {
        private val MIN_RECORDING_DURATION = 2000.milliseconds
        private val MAX_RECORDING_DURATION = 6.minutes
        private val TIMER_UPDATE_FREQUENCY = 100.milliseconds
        private const val VOICE_TOOLTIP_CLICKS_COUNT = 3
        private const val AUDIO_MP3_EXTENSION = "mp3"
    }
}
