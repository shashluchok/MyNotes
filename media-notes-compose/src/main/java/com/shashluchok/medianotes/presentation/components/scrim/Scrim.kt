package com.shashluchok.medianotes.presentation.components.scrim

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

@Composable
internal fun Scrim(
    color: Color,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween()
    )

    Canvas(
        modifier.then(
            if (isVisible) {
                Modifier.pointerInput(onClick) { detectTapGestures { onClick() } }
            } else {
                Modifier
            }
        )
    ) {
        drawRect(color = color, alpha = alpha.coerceIn(0f, 1f))
    }
}
