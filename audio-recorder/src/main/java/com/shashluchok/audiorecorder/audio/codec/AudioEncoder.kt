package com.shashluchok.audiorecorder.audio.codec

interface AudioEncoder {
    fun encode(inputRawBuf: ShortArray, samplesCount: Int, outputResultBuf: ByteArray):Int
    fun flush(): Int
    fun close()
}