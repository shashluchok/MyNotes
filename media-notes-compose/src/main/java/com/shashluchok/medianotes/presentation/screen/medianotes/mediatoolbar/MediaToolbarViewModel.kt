package com.shashluchok.medianotes.presentation.screen.medianotes.mediatoolbar

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.shashluchok.audiorecorder.audio.AudioRecorderImpl
import com.shashluchok.audiorecorder.audio.FileDataSource
import com.shashluchok.medianotes.data.MediaNote
import com.shashluchok.medianotes.domain.notes.create.CreateMediaNoteInteractor
import com.shashluchok.medianotes.presentation.screen.AbsViewModel
import com.shashluchok.medianotes.presentation.screen.medianotes.MediaNoteItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class MediaToolbarViewModel(
    private val createMediaNote: CreateMediaNoteInteractor
) : AbsViewModel<MediaToolbarViewModel.State>() {

    data class RecordingState(
        val timerMillis: Long = 0L,
        val volumeLevel: Float = 0F
    )

    data class State(
        val tooltipVisible: Boolean = false,
        val recordingState: RecordingState? = null,
        val text: String = "",
        val editableNote: MediaNoteItem.Text? = null
    ) {
        val isEditing: Boolean = editableNote != null
    }

    private val recorder = AudioRecorderImpl()

    private var timerJob: Job? = null

    private var voiceClicks = 0

    private var file: File? = null

    sealed interface Action {
        data object OnVoiceClick : Action
        data class OnVoiceLongClick(val context: Context) : Action
        data object OnVoiceDragEnd : Action
        data object OnVoiceDragCancel : Action
        data object OnSendClick : Action
        data class OnTextChange(val text: String) : Action
        data object OnToolTipDismissRequest : Action
    }

    override val mutableStateFlow: MutableStateFlow<State> = MutableStateFlow(
        State()
    )

    override fun onCleared() {
        timerJob?.cancel()
        recorder.destroy()
        super.onCleared()
    }

    fun onTextNoteToEdit(textNote: MediaNoteItem.Text?) {
        mutableStateFlow.update {
            it.copy(
                editableNote = textNote,
                text = textNote?.value ?: ""
            )
        }
    }

    fun onAction(action: Action) {
        when (action) {
            Action.OnSendClick -> onSendClick()
            is Action.OnTextChange -> onTextChange(action.text)
            Action.OnToolTipDismissRequest -> onDismissToolTip()
            Action.OnVoiceClick -> onVoiceClick()
            Action.OnVoiceDragCancel -> onStopRecording(save = false)
            Action.OnVoiceDragEnd -> onStopRecording(save = true)
            is Action.OnVoiceLongClick -> onStartRecording(context = action.context)
        }
    }

    private fun onSendClick() {
        viewModelScope.launch {
            createMediaNote(
                MediaNote.Text(
                    value = state.text
                )
            ).also {
                mutableStateFlow.update { it.copy(text = "") }
            }
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
                    timerJob?.cancel()
                    timerJob = launch {
                        val initialTime = System.currentTimeMillis()
                        mutableStateFlow.update {
                            it.copy(
                                recordingState = RecordingState()
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
                        }
                    }
                }
            )
        }
    }

    private fun onStopRecording(save: Boolean) {
        viewModelScope.launch {
            println("Zhoppa STOP $save")
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

    private fun onTextChange(text: String) {
        state = state.copy(
            text = text
        )
    }

    private fun showRecordingToolTip() {
        state = state.copy(
            tooltipVisible = false
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

    companion object {
        private val MIN_RECORDING_DURATION = 1000.milliseconds
        private val TIMER_UPDATE_FREQUENCY = 100.milliseconds
        private const val VOICE_TOOLTIP_CLICKS_COUNT = 3
        private const val AUDIO_MP3_EXTENSION = "mp3"
    }
}
