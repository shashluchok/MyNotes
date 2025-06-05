package com.shashluchok.medianotes.presentation.components.cropper.cropper.crop

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.shashluchok.medianotes.presentation.components.cropper.cropper.model.CropData
import com.shashluchok.medianotes.presentation.components.cropper.cropper.model.CropShape

internal class CropAgent {

    private val imagePaint = Paint().apply {
        blendMode = BlendMode.SrcIn
    }

    fun crop(
        cropData: CropData,
        layoutDirection: LayoutDirection,
        density: Density
    ): ImageBitmap {
        val croppedBitmap: Bitmap = Bitmap.createBitmap(
            cropData.imageBitmap.asAndroidBitmap(),
            cropData.cropRect.left.toInt(),
            cropData.cropRect.top.toInt(),
            cropData.cropRect.width.toInt(),
            cropData.cropRect.height.toInt()
        )

        val imageToCrop = croppedBitmap
            .copy(Bitmap.Config.ARGB_8888, true)
            .asImageBitmap()

        drawCroppedImage(
            cropData.cropShape,
            cropData.cropRect,
            layoutDirection,
            density,
            imageToCrop
        )

        return imageToCrop
    }

    private fun drawCroppedImage(
        cropShape: CropShape,
        cropRect: Rect,
        layoutDirection: LayoutDirection,
        density: Density,
        imageToCrop: ImageBitmap
    ) {
        val path = Path().apply {
            val outline = cropShape.shape.createOutline(cropRect.size, layoutDirection, density)
            addOutline(outline)
        }

        Canvas(image = imageToCrop).run {
            saveLayer(nativeCanvas.clipBounds.toComposeRect(), imagePaint)

            // Destination
            drawPath(path, Paint())

            // Source
            drawImage(
                image = imageToCrop,
                topLeftOffset = Offset.Zero,
                paint = imagePaint
            )
            restore()
        }
    }
}
