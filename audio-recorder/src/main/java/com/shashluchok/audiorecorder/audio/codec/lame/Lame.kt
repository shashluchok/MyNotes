package com.shashluchok.audiorecorder.audio.codec.lame

open class Lame {

    init {
        System.loadLibrary("liblame-jni")
    }

    external fun initLameEncoder(sampleRate: Int): Long
    external fun encode(lamePtr: Long, inputData: ShortArray?, samples: Int, outputData: ByteArray): Int
    external fun flush(lamePtr: Long, outputData: ByteArray?): Int
    external fun closeLame(lamePtr: Long)
    external fun getVersion(): String

}