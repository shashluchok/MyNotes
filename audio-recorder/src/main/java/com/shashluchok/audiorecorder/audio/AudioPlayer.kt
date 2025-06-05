package com.shashluchok.audiorecorder.audio

import kotlinx.coroutines.flow.MutableStateFlow

interface AudioPlayer {

    data class PlayInfo(
        val progress: Float,
        val remainingDuration: Int
    )

    enum class PlayerState {
        INITIALIZED, RELEASED, COMPLETED, PAUSED,
        SEEKING, PLAYING, STOPPED,
    }

    val state: MutableStateFlow<PlayerState>
    val playInfoState: MutableStateFlow<PlayInfo?>

    fun play()
    fun pause()
    fun stop()
    fun seek(timeStamp: Int)
    fun destroy()
    fun load(dataSource: FileDataSource)
}