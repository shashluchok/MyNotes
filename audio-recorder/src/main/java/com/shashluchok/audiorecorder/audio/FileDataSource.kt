package com.shashluchok.audiorecorder.audio

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class FileDataSource(
    val file: File
) : DataSource {

    override fun openInputStream() = FileInputStream(file)

    override fun openOutputStream(append: Boolean) = FileOutputStream(file, append)

    override val size = if (file.exists()) file.length() else 0
}
