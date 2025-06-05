package com.shashluchok.medianotes.presentation.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

internal val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope> {
    throw IllegalStateException("No SharedTransitionScope provided")
}

@OptIn(ExperimentalSharedTransitionApi::class)
internal val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope> {
    throw IllegalStateException("No SharedTransitionScope provided")
}

internal val LocalIsCompact = compositionLocalOf { false }
