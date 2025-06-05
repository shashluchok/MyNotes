package com.shashluchok.medianotes.presentation.components.cropper.cropper.draw

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity

@Composable
internal fun ImageDrawCanvas(
    imageBitmap: ImageBitmap,
    filterQuality: FilterQuality,
    modifier: Modifier = Modifier
) {
    with(LocalDensity.current) {
        Canvas(
            modifier = modifier.requiredSize(
                width = imageBitmap.width.toDp(),
                height = imageBitmap.height.toDp()
            )
        ) {
            drawImage(
                image = imageBitmap,
                filterQuality = filterQuality
            )
        }
    }
}
