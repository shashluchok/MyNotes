package com.shashluchok.medianotes.domain.file

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

internal class SaveBitmapToFileInteractorImpl : SaveBitmapToFileInteractor {

    override suspend operator fun invoke(
        context: Context,
        bitmap: ImageBitmap,
        fileName: String,
        fileFormat: Bitmap.CompressFormat
    ) = withContext(Dispatchers.IO) {
        try {
            val directory = File(context.filesDir, IMAGES_DIR_NAME)
            if (!directory.exists()) directory.mkdirs()

            val file = File(directory, "$fileName.${fileFormat.name.lowercase()}")
            val outputStream = FileOutputStream(file)

            bitmap.asAndroidBitmap().compress(fileFormat, COMPRESSION_QUALITY, outputStream)
            outputStream.flush()
            outputStream.close()

            Result.success(file)
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Save bitmap to file failed: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    companion object {
        private const val LOG_TAG = "SaveBitmapToFile"
        private const val IMAGES_DIR_NAME = "images"
        private const val COMPRESSION_QUALITY = 100
    }
}
