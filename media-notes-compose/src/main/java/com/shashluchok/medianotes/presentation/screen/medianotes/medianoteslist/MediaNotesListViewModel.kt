package com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist

import android.content.res.Resources
import androidx.lifecycle.viewModelScope
import com.shashluchok.audiorecorder.audio.AudioPlayer
import com.shashluchok.audiorecorder.audio.AudioPlayer.PlayerState.COMPLETED
import com.shashluchok.audiorecorder.audio.AudioPlayer.PlayerState.INITIALIZED
import com.shashluchok.audiorecorder.audio.AudioPlayer.PlayerState.PAUSED
import com.shashluchok.audiorecorder.audio.AudioPlayer.PlayerState.PLAYING
import com.shashluchok.audiorecorder.audio.AudioPlayer.PlayerState.RELEASED
import com.shashluchok.audiorecorder.audio.AudioPlayer.PlayerState.SEEKING
import com.shashluchok.audiorecorder.audio.AudioPlayer.PlayerState.STOPPED
import com.shashluchok.audiorecorder.audio.FileDataSource
import com.shashluchok.audiorecorder.audio.codec.mpg123.Mpg123Decoder
import com.shashluchok.medianotes.domain.notes.get.GetMediaNotesInteractor
import com.shashluchok.medianotes.presentation.screen.AbsViewModel
import com.shashluchok.medianotes.presentation.screen.medianotes.data.MediaNoteItem
import com.shashluchok.medianotes.presentation.screen.medianotes.data.toMediaNoteItem
import com.shashluchok.medianotes.presentation.utils.toAudioDisplayString
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class MediaNotesListViewModel(
    private val getMediaNotesInteractor: GetMediaNotesInteractor,
    private val resources: Resources,
    private val player: AudioPlayer
) : AbsViewModel<MediaNotesListViewModel.State>() {

    data class PlayVoiceInfo(
        val mediaNote: MediaNoteItem.Voice,
        val paused: Boolean = false,
        val progress: Float = 0f,
        val remainingDuration: String = mediaNote.duration
    )

    data class State(
        val notes: ImmutableList<MediaNoteItem> = persistentListOf(),
        val playingVoiceInfo: PlayVoiceInfo? = null
    )

    override val mutableStateFlow: MutableStateFlow<State> = MutableStateFlow(State())

    private val decoder = Mpg123Decoder()

    init {
        subscribeToMediaNotes()

        viewModelScope.launch {
            player.playInfoState.collect { info ->
                info?.let {
                    mutableStateFlow.update {
                        it.copy(
                            playingVoiceInfo = it.playingVoiceInfo?.copy(
                                progress = info.progress,
                                remainingDuration = info.remainingDuration.toDuration(
                                    unit = DurationUnit.MILLISECONDS
                                ).toAudioDisplayString()
                            )
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            player.state.collect { playerState ->
                when (playerState) {
                    INITIALIZED -> {
                        player.play()
                    }

                    COMPLETED -> {
                        mutableStateFlow.update {
                            it.copy(playingVoiceInfo = null)
                        }
                    }

                    PAUSED -> {
                        mutableStateFlow.update {
                            it.copy(
                                playingVoiceInfo = it.playingVoiceInfo?.copy(
                                    paused = true

                                )
                            )
                        }
                    }

                    PLAYING -> {
                        mutableStateFlow.update {
                            it.copy(
                                playingVoiceInfo = it.playingVoiceInfo?.copy(
                                    paused = false
                                )
                            )
                        }
                    }

                    STOPPED, RELEASED, SEEKING -> Unit
                }
            }
        }
    }

    sealed interface Action {
        data class OnSeekStart(
            val mediaNote: MediaNoteItem.Voice,
            val progress: Float
        ) : Action

        data class OnSeek(
            val mediaNote: MediaNoteItem.Voice,
            val progress: Float
        ) : Action

        data class OnSeekEnd(val mediaNote: MediaNoteItem.Voice) : Action

        data class OnPlayClick(val mediaNote: MediaNoteItem.Voice) : Action
        data object OnAppBackground : Action
    }

    fun onAction(action: Action) {
        when (action) {
            Action.OnAppBackground -> onAppBackground()
            is Action.OnPlayClick -> onPlayClick(action.mediaNote)
            is Action.OnSeek -> onSeek(mediaNote = action.mediaNote, progress = action.progress)
            is Action.OnSeekEnd -> onSeekEnd(action.mediaNote)
            is Action.OnSeekStart -> onSeekStart(mediaNote = action.mediaNote, progress = action.progress)
        }
    }

    override fun onCleared() {
        decoder.close()
        super.onCleared()
    }

    private fun subscribeToMediaNotes() {
        viewModelScope.launch {
            getMediaNotesInteractor.mediaNotesFlow.collect { notes ->
                state.playingVoiceInfo?.mediaNote?.let { playingNote ->
                    if (notes.filter { it.id == playingNote.id }.isEmpty()) {
                        player.stop()
                    }
                }
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

    private fun onSeekStart(mediaNote: MediaNoteItem.Voice, progress: Float) {
        state.playingVoiceInfo?.let {
            if (it.mediaNote != mediaNote) return
            player.pause()
            player.seek(
                (it.mediaNote.durationMillis * progress).toInt()
            )
        }
    }

    private fun onSeekEnd(mediaNote: MediaNoteItem.Voice) {
        state.playingVoiceInfo?.let {
            if (it.mediaNote != mediaNote) return
            player.play()
        }
    }

    private fun onSeek(mediaNote: MediaNoteItem.Voice, progress: Float) {
        state.playingVoiceInfo?.let {
            if (it.mediaNote != mediaNote) return
            player.seek(
                (it.mediaNote.durationMillis * progress).toInt()
            )
        }
    }

    private fun onPlayClick(mediaNote: MediaNoteItem.Voice) {
        viewModelScope.launch {
            val playVoiceInfo = state.playingVoiceInfo

            if (playVoiceInfo != null && playVoiceInfo.mediaNote == mediaNote) {
                if (playVoiceInfo.paused) {
                    player.play()
                } else {
                    player.pause()
                }
            } else {
                playVoiceInfo?.let {
                    player.stop()
                }

                mutableStateFlow.update {
                    it.copy(
                        playingVoiceInfo = PlayVoiceInfo(
                            mediaNote
                        )
                    )
                }
                player.load(FileDataSource(file = File(mediaNote.path)))
            }
        }
    }

    private fun onAppBackground() {
        player.pause()
    }
}
