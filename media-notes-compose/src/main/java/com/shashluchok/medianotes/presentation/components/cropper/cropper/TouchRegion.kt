package com.shashluchok.medianotes.presentation.components.cropper.cropper

internal enum class TouchRegion {
    TopLeft, TopRight, BottomLeft, BottomRight, Inside, None
}

internal fun handlesTouched(touchRegion: TouchRegion) =
    touchRegion == TouchRegion.TopLeft ||
        touchRegion == TouchRegion.TopRight ||
        touchRegion == TouchRegion.BottomLeft ||
        touchRegion == TouchRegion.BottomRight
