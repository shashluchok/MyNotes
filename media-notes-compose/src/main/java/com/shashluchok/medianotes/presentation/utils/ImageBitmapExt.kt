package com.shashluchok.medianotes.presentation.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap

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

internal fun ImageBitmap.withWhiteBackground(): ImageBitmap {
    val original = this.asAndroidBitmap().toSoftwareBitmap()
    val newBitmap = createBitmap(original.width, original.height)
    val canvas = Canvas(newBitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    canvas.drawBitmap(original, 0f, 0f, null)
    return newBitmap.asImageBitmap()
}

private fun Bitmap.toSoftwareBitmap(): Bitmap {
    if (config != Bitmap.Config.HARDWARE) return this
    return copy(Bitmap.Config.ARGB_8888, true)
}
