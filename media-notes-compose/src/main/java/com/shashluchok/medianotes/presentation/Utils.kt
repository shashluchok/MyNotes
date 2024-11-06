package com.shashluchok.medianotes.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
internal fun Dp.toPx(): Float {
    return LocalDensity.current.run { toPx() }
}

@Composable
internal fun Float.toDp(): Dp {
    return LocalDensity.current.run { toDp() }
}
