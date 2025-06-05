package com.shashluchok.medianotes.presentation.utils

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

internal val ClosedRange<Dp>.middle: Dp
    get() = (endInclusive - start) / 2 + start
