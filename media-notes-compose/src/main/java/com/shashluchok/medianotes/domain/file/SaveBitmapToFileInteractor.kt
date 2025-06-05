package com.shashluchok.medianotes.domain.file

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.datetime.Clock
import java.io.File

internal interface SaveBitmapToFileInteractor {
    suspend operator fun invoke(
        context: Context,
        bitmap: ImageBitmap,
        fileName: String = Clock.System.now().epochSeconds.toString(),
        fileFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG
    ): Result<File>
}
