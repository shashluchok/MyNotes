package com.shashluchok.audiorecorder.audio

import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import com.shashluchok.audiorecorder.audio.AudioPlayer.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlayerImpl(
    scope: CoroutineScope
) : AudioPlayer, OnCompletionListener, OnPreparedListener, MediaPlayer.OnErrorListener {

    private val mediaPlayer = MediaPlayer()

    override val state = MutableStateFlow(PlayerState.RELEASED)
    override val playInfoState: MutableStateFlow<AudioPlayer.PlayInfo?> = MutableStateFlow(null)

    init {
        scope.launch(Dispatchers.Default) {
            while (isActive) {
                if (state.value != PlayerState.RELEASED) {
                    playInfoState.update {
                        AudioPlayer.PlayInfo(
                            progress = currentProgress,
                            remainingDuration = remainingDuration
                        )
                    }
                }
            }
        }
    }

    override fun load(dataSource: FileDataSource) {
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnErrorListener(this)
        try {
            mediaPlayer.reset()
            state.value = PlayerState.RELEASED
            mediaPlayer.setDataSource(dataSource.openInputStream().fd)
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            // ...
        }
    }

    override fun play() {
        if (state.value != PlayerState.RELEASED && !mediaPlayer.isPlaying) {
            mediaPlayer.start()
            state.update {
                PlayerState.PLAYING
            }
        }
    }

    override fun pause() {
        if (state.value != PlayerState.RELEASED && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            state.update {
                PlayerState.PAUSED
            }
        }
    }

    override fun stop() {
        if (state.value != PlayerState.RELEASED) {
            mediaPlayer.stop()
            state.update {
                PlayerState.STOPPED
            }
        }
    }

    override fun seek(timeStamp: Int) {
        if (state.value != PlayerState.RELEASED) {
            mediaPlayer.seekTo(timeStamp)
            state.update {
                PlayerState.SEEKING
            }
        }
    }

    override fun destroy() {
        mediaPlayer.release()
        state.update { PlayerState.RELEASED }
    }

    override fun onPrepared(mp: MediaPlayer) {
        state.update { PlayerState.INITIALIZED }
    }

    override fun onCompletion(mp: MediaPlayer) {
        mediaPlayer.stop()
        state.value = PlayerState.COMPLETED
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        // ...
        return true
    }

    private val currentProgress: Float
        get() {
            return if (state.value != PlayerState.RELEASED) {
                mediaPlayer.currentPosition / mediaPlayer.duration.toFloat()
            } else {
                0f
            }
        }

    private val remainingDuration: Int
        get() {
            return if (state.value != PlayerState.RELEASED) {
                mediaPlayer.duration - mediaPlayer.currentPosition
            } else {
                0
            }
        }
}
