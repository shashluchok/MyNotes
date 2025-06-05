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

    /*override suspend fun calculateVolumeLevelsAndDuration(
        fileDataSource: FileDataSource,
        analyzer: AudioAnalyzer
    ): Pair<ImmutableList<Float>, Duration>  {
        return withContext(Dispatchers.IO) {
            val duration = getDuration(fileDataSource.file.path)
            val bufferSize = 4096
            val peaksCount = analyzer.getResultPeaksCount()

            val pcmBufferL = ShortArray(bufferSize)
            val pcmBufferR = ShortArray(bufferSize)
            val mp3Buffer = ByteArray(bufferSize)

            val resultPeaks = FloatArray(peaksCount)
            var peakCounter = 0

            var sampleCounter = 0
            var sum = 0f
            var mp3Len: Int

            var totalFrames: Long = 0

            var read: Int
            var samples: Int
            val inputStream = fileDataSource.openInputStream()

            try {
                while ((inputStream.read(mp3Buffer, 0, bufferSize).also { read = it }) > 0) {
                    mp3Len = read
                    do {
                        samples = decode(mp3Buffer, mp3Len, pcmBufferL, pcmBufferR)
                        if (samples > 0 && !isParsed()) {
                            break
                        }
                        if (samples > 0) {
                            if (totalFrames == 0L) {
                                totalFrames = calcTotalFramesCount(
                                    fileDataSource.size,
                                )
                            }
                            sum += analyzer.getMaxLevelFromSample(pcmBufferL, samples)
                            sampleCounter++
                            if (sampleCounter >= totalFrames / peaksCount) {
                                val peak = sum / sampleCounter
                                resultPeaks[peakCounter] = peak
                                peakCounter++
                                if (peakCounter == peaksCount) {
                                    println("Zhoppa duration 1 = $duration")
                                    return@withContext resultPeaks.toList().toImmutableList() to
                                        duration.toDuration(DurationUnit.SECONDS)
                                }
                                sum = 0f
                                sampleCounter = 0
                            }
                        }
                        mp3Len = 0
                    } while (samples > 0)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                close()
                try {
                    inputStream.close()
                } catch (ignored: IOException) {
                }
            }
            println("Zhoppa duration 2 = $duration")
            return@withContext resultPeaks.toList().toImmutableList() to
                duration.toDuration(DurationUnit.SECONDS)
        }


    }

    private fun calcTotalFramesCount(
        fileSize: Long,
    ): Long {
        if (getFramesCount(mpgPtr) == 0 && getFrameSize(mpgPtr) > 0 && getBitrate(mpgPtr) > 0) {
            return (fileSize / (getBitrate(mpgPtr) / 8)
                * (getSampleRate(mpgPtr) / 1000)) / getFrameSize(mpgPtr)
        }
        return getFramesCount(mpgPtr).toLong()
    }*/
}
