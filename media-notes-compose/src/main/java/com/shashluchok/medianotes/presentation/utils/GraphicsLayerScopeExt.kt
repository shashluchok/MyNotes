package com.shashluchok.medianotes.presentation.utils

import androidx.compose.ui.graphics.GraphicsLayerScope
import com.shashluchok.medianotes.presentation.components.cropper.cropper.state.CropState

internal fun GraphicsLayerScope.update(cropState: CropState) {
    val zoom = cropState.zoom
    this.scaleX = zoom
    this.scaleY = zoom

    val pan = cropState.pan
    val translationX = pan.x
    val translationY = pan.y
    this.translationX = translationX
    this.translationY = translationY

    this.rotationZ = cropState.rotation % 360f
}
