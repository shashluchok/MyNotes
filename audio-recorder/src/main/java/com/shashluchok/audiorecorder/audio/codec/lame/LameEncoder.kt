package com.shashluchok.audiorecorder.audio.codec.lame

import com.shashluchok.audiorecorder.audio.codec.AudioEncoder

class LameEncoder(
    private val sampleRate: Int
) : Lame(), AudioEncoder {

    private var lamePtr: Long = 0
    private var lastBuffer: ByteArray? = null

    override fun encode(inputRawBuf: ShortArray, samplesCount: Int, outputResultBuf: ByteArray): Int {
        if (lamePtr == 0L) {
            lamePtr = initLameEncoder(sampleRate)
            lastBuffer = null
        }
        val result = encode(lamePtr, inputRawBuf, samplesCount, outputResultBuf)
        lastBuffer = outputResultBuf
        return result
    }

    override fun flush(): Int {
        return if (lamePtr != 0L) {
            flush(lamePtr, lastBuffer)
        } else -1
    }

    override fun close() {
        if (lamePtr != 0L) {
            closeLame(lamePtr)
            lamePtr = 0L
        }
    }
}