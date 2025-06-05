package com.shashluchok.audiorecorder.audio.codec

import com.shashluchok.audiorecorder.audio.FileDataSource
import kotlinx.collections.immutable.ImmutableList
import kotlin.time.Duration

interface AudioDecoder {
    fun decode(
        inputEncodedBuf: ByteArray,
        samplesCount: Int,
        outputResultBufL: ShortArray,
        outputResultBufR: ShortArray
    ): Int

    fun close()

    suspend fun calculateVolumeLevelsAndDuration(
        fileDataSource: FileDataSource
    ): Pair<ImmutableList<Float>, Duration>
}
