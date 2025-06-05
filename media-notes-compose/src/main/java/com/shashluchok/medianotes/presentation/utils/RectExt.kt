package com.shashluchok.medianotes.presentation.utils

import androidx.compose.ui.geometry.Rect

internal fun Rect.contains(rect: Rect): Boolean {
    return rect.left >= left &&
        rect.right <= right &&
        rect.top >= top &&
        rect.bottom <= bottom
}
