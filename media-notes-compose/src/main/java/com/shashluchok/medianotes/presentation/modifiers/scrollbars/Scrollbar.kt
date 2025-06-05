package com.shashluchok.medianotes.presentation.modifiers.scrollbars

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shashluchok.medianotes.presentation.modifiers.scrollbars.ScrollbarDimensions.Companion.toDimensions

internal fun Modifier.scrollbar(
    state: LazyListState,
    config: ScrollbarConfig = ScrollbarConfig(),
    alwaysVisible: Boolean = false
) = composed {
    val drawer = remember(config) {
        LazyListScrollbarDrawer(state)
    }
    scrollbar(state.isScrollInProgress, drawer, config, alwaysVisible)
}

internal fun Modifier.horizontalScrollbar(
    state: ScrollState,
    config: ScrollbarConfig = ScrollbarConfig(),
    alwaysVisible: Boolean = false
) = composed {
    val drawer = remember(Orientation.Horizontal, config) {
        DefaultScrollbarDrawer(state, Orientation.Horizontal)
    }
    scrollbar(state.isScrollInProgress, drawer, config, alwaysVisible)
}

internal fun Modifier.verticalScrollbar(
    state: ScrollState,
    config: ScrollbarConfig = ScrollbarConfig(),
    alwaysVisible: Boolean = false
) = composed {
    val drawer = remember(Orientation.Vertical, config) {
        DefaultScrollbarDrawer(state, Orientation.Vertical)
    }
    scrollbar(state.isScrollInProgress, drawer, config, alwaysVisible)
}

internal fun Modifier.horizontalScrollbar(
    state: LazyGridState,
    spans: Int,
    config: ScrollbarConfig = ScrollbarConfig(),
    alwaysVisible: Boolean = false
) = composed {
    val drawer = remember(Orientation.Horizontal, spans, config) {
        LazyGridScrollbarDrawer(state, Orientation.Horizontal, spans)
    }
    scrollbar(state.isScrollInProgress, drawer, config, alwaysVisible)
}

internal fun Modifier.verticalScrollbar(
    state: LazyGridState,
    spans: Int,
    config: ScrollbarConfig = ScrollbarConfig(),
    alwaysVisible: Boolean = false
) = composed {
    val drawer = remember(Orientation.Vertical, spans, config) {
        LazyGridScrollbarDrawer(state, Orientation.Vertical, spans)
    }
    scrollbar(state.isScrollInProgress, drawer, config, alwaysVisible)
}

private fun Modifier.scrollbar(
    isScrollInProgress: Boolean,
    drawer: ScrollbarDrawer,
    config: ScrollbarConfig = ScrollbarConfig(),
    alwaysVisible: Boolean = true
) = composed {
    val color = MaterialTheme.colorScheme.outlineVariant

    if (alwaysVisible) {
        drawWithContent {
            drawContent()
            val dimensions = config.toDimensions(this)
            with(drawer) {
                drawScrollbar(
                    color = color,
                    alpha = 1f,
                    dimensions = dimensions
                )
            }
        }
    } else {
        val duration = when (isScrollInProgress) {
            true -> config.fadeInDurationMillis
            false -> config.fadeOutDurationMillis
        }
        val alpha by animateFloatAsState(
            targetValue = if (isScrollInProgress) 1f else 0f,
            animationSpec = tween(
                durationMillis = duration,
                delayMillis = if (isScrollInProgress) 0 else config.scrollbarFadeOutDelay
            )
        )

        drawWithContent {
            drawContent()
            if (alpha != 0f) {
                val dimensions = config.toDimensions(this)
                with(drawer) {
                    drawScrollbar(
                        color = color,
                        alpha = alpha,
                        dimensions = dimensions
                    )
                }
            }
        }
    }
}

internal data class ScrollbarConfig internal constructor(
    val thickness: Dp = 4.dp,
    val padding: PaddingValues = PaddingValues(2.dp),
    val fadeInDurationMillis: Int = 150,
    val fadeOutDurationMillis: Int = 500,
    val scrollbarFadeOutDelay: Int = 200
)
