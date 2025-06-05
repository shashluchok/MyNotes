package com.shashluchok.audiorecorder.audio.codec.mpg123

open class Mpg123 {
    init {
        System.loadLibrary("libmpg-jni")
    }

    external fun init(): Long
    external fun decode(
        mpgPtr: Long,
        inputData: ByteArray?,
        samples: Int,
        outputDataL: ShortArray,
        outputDataR: ShortArray
    ): Int
    external fun getDuration(filePath: String): Double
    external fun getVolumePeaks(filePath: String, numPeaks: Int): FloatArray

    external fun close(mpgPtr: Long)


}