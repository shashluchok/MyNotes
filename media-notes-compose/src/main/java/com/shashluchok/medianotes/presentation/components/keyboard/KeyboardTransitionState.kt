package com.shashluchok.medianotes.presentation.components.keyboard

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity

internal data class KeyboardTransitionState(
    val isOpening: Boolean
)

@Composable
internal fun rememberKeyboardTransitionState(): KeyboardTransitionState {
    val density = LocalDensity.current
    val ime = WindowInsets.ime

    var lastIme by remember {
        mutableIntStateOf(0)
    }

    var isImeOpening by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(ime) {
        snapshotFlow { ime.getBottom(density) }.collect {
            isImeOpening = it > lastIme
            lastIme = it
        }
    }

    return remember(isImeOpening) {
        KeyboardTransitionState(isImeOpening)
    }
}
