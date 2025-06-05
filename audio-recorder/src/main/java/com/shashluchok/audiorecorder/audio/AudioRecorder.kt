package com.shashluchok.audiorecorder.audio

interface AudioRecorder {
    suspend fun record(dataSource: FileDataSource, onStart: () -> Unit, onNewVolume: (Float) -> Unit)
    fun stop()
    fun destroy()
}