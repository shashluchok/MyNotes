package com.shashluchok.audiorecorder.audio

import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {

    data class PlayInfo(
        val progress: Float,
        val remainingDuration: Int
    )

    enum class PlayerState {
        INITIALIZED, RELEASED, COMPLETED, PAUSED,
        SEEKING, PLAYING, STOPPED,
    }

    val state: StateFlow<PlayerState>
    val playInfoState: StateFlow<PlayInfo?>

    fun play()
    fun pause()
    fun stop()
    fun seek(timeStamp: Int)
    fun destroy()
    fun load(dataSource: FileDataSource)
}
