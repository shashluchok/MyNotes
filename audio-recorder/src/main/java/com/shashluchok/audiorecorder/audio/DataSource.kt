package com.shashluchok.audiorecorder.audio

import java.io.InputStream
import java.io.OutputStream

interface DataSource {
    fun openInputStream(): InputStream
    fun openOutputStream(append: Boolean): OutputStream
    val size: Long
}