package com.shashluchok.medianotes.presentation.components.camera

import android.graphics.Bitmap
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private const val defaultBlurRatio = 20f

@Composable
internal fun BlurImage(
    bitmap: Bitmap,
    modifier: Modifier = Modifier,
    blurRatio: Float = defaultBlurRatio
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        val renderScript = RenderScript.create(LocalContext.current)
        val bitmapAlloc = Allocation.createFromBitmap(renderScript, bitmap)
        ScriptIntrinsicBlur.create(renderScript, bitmapAlloc.element).apply {
            setRadius(blurRatio)
            setInput(bitmapAlloc)
            forEach(bitmapAlloc)
        }
        bitmapAlloc.copyTo(bitmap)
        renderScript.destroy()

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.blur(blurRatio.dp)
        )
    }
}
