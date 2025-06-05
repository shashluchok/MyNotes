package com.shashluchok.audiorecorder.audio.codec.mpg123

import com.shashluchok.audiorecorder.audio.FileDataSource
import com.shashluchok.audiorecorder.audio.codec.AudioDecoder
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class Mpg123Decoder : Mpg123(), AudioDecoder {

    private var mpgPtr = init()

    override fun decode(
        inputEncodedBuf: ByteArray,
        samplesCount: Int,
        outputResultBufL: ShortArray,
        outputResultBufR: ShortArray
    ): Int {
        if (mpgPtr == 0L) {
            mpgPtr = init()
        }
        return decode(mpgPtr, inputEncodedBuf, samplesCount, outputResultBufL, outputResultBufR)
    }

    override fun close() {
        if (mpgPtr != 0L) {
            close(mpgPtr)
            mpgPtr = 0
        }
    }

    override suspend fun calculateVolumeLevelsAndDuration(
        fileDataSource: FileDataSource
    ): Pair<ImmutableList<Float>, Duration> {
        return withContext(Dispatchers.IO) {
            val duration = getDuration(fileDataSource.file.path).toDuration(DurationUnit.SECONDS)
            val peaks = getVolumePeaks(fileDataSource.file.path, 100).toList().toImmutableList()
            peaks to duration
        }
    }
}
