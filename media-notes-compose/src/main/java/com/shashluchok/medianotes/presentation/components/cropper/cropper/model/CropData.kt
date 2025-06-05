package com.shashluchok.medianotes.presentation.components.cropper.cropper.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap

@Immutable
internal data class CropData(
    val zoom: Float = 1f,
    val pan: Offset = Offset.Zero,
    val rotation: Float = 0f,
    val overlayRect: Rect = Rect.Zero,
    val cropRect: Rect = Rect.Zero,
    val cropShape: CropShape,
    val imageBitmap: ImageBitmap
)
