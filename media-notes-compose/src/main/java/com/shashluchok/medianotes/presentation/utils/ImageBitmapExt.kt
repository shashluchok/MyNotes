package com.shashluchok.medianotes.presentation.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap

internal fun ImageBitmap.transform(matrix: Matrix): ImageBitmap {
    return Bitmap.createBitmap(
        asAndroidBitmap(),
        0,
        0,
        width,
        height,
        matrix,
        true
    ).asImageBitmap()
}
