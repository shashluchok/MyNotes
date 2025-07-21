package com.shashluchok.audiorecorder.audio

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.shashluchok.audiorecorder.audio.AudioPlayer.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class AudioPlayerImpl(
    private val scope: CoroutineScope,
    context: Context
) : AudioPlayer {

    override val state = MutableStateFlow(PlayerState.RELEASED)
    override val playInfoState: MutableStateFlow<AudioPlayer.PlayInfo?> = MutableStateFlow(null)

    private var isLoaded = false

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build()
    }

    init {

        scope.launch(Dispatchers.Default) {
            while (isActive) {
                if (state.value != PlayerState.RELEASED && isLoaded) {
                    withContext(Dispatchers.Main) {
                        playInfoState.update {
                            AudioPlayer.PlayInfo(
                                progress = currentProgress,
                                remainingDuration = remainingDuration
                            )
                        }
                    }
                }
                delay(PLAYER_STATE_INFO_UPDATE_PERIOD)
            }
        }

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                state.value = when (playbackState) {
                    Player.STATE_ENDED -> PlayerState.COMPLETED
                    else -> state.value
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                state.value = PlayerState.RELEASED
            }
        })
    }

    override fun load(dataSource: FileDataSource) {
        scope.launch(Dispatchers.Main) {
            try {
                val uri = Uri.fromFile(dataSource.file)
                val mediaItem = MediaItem.fromUri(uri)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                isLoaded = true
                state.value = PlayerState.INITIALIZED
            } catch (e: Exception) {
                state.value = PlayerState.RELEASED
                isLoaded = false
            }
        }
    }

    override fun play() {
        scope.launch(Dispatchers.Main) {
            if (isLoaded) {
                exoPlayer.play()
                state.value = PlayerState.PLAYING
            }
        }
    }

    override fun pause() {
        scope.launch(Dispatchers.Main) {
            if (isLoaded && exoPlayer.isPlaying) {
                exoPlayer.pause()
                state.value = PlayerState.PAUSED
            }
        }
    }

    override fun stop() {
        scope.launch(Dispatchers.Main) {
            if (isLoaded) {
                exoPlayer.stop()
                state.value = PlayerState.STOPPED
            }
        }
    }

    override fun seek(timeStamp: Int) {
        scope.launch(Dispatchers.Main) {
            if (isLoaded) {
                exoPlayer.seekTo(timeStamp.toLong())
                state.value = PlayerState.SEEKING
            }
        }
    }

    override fun destroy() {
        scope.launch(Dispatchers.Main) {
            exoPlayer.release()
            isLoaded = false
            state.value = PlayerState.RELEASED
        }
    }

    private val currentProgress: Float
        get() = if (isLoaded && exoPlayer.duration > 0) {
            exoPlayer.currentPosition / exoPlayer.duration.toFloat()
        } else {
            0f
        }

    private val remainingDuration: Int
        get() = if (isLoaded) {
            (exoPlayer.duration - exoPlayer.currentPosition).toInt()
        } else {
            0
        }

    companion object {
        private val PLAYER_STATE_INFO_UPDATE_PERIOD = 10.milliseconds
    }
}
