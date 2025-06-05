package com.shashluchok.medianotes.presentation.screen.medianotes.mediatoolbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val stowWatchIconSize = 12.dp
private val stowWatchHorizontalArrangement = Arrangement.spacedBy(4.dp)

private const val stowWatchIconFadeDuration = 1000
private const val stowWatchIconMinAlpha = 0.1f
private const val stowWatchIncrementAnimationLabel = "timerIncrementAnimation"
private const val stowWatchTimeFormat = "%01d:%02d,%1d"

@Composable
internal fun StopWatch(
    timerValueMillis: Long,
    modifier: Modifier = Modifier
) {
    var iconAlpha by remember {
        mutableFloatStateOf(1f)
    }

    // Every second starts new timer icon blink animation
    LaunchedEffect(timerValueMillis / 1.seconds.inWholeMilliseconds) {
        animate(
            initialValue = 1f,
            targetValue = stowWatchIconMinAlpha,
            animationSpec = tween(
                durationMillis = stowWatchIconFadeDuration,
                easing = FastOutSlowInEasing
            ),
            block = { value, _ -> iconAlpha = value }
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = stowWatchHorizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .size(stowWatchIconSize),
            painter = rememberVectorPainter(Icons.Filled.FiberManualRecord),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error.copy(
                alpha = iconAlpha
            )
        )
        Row {
            with(timerValueMillis.formatTime()) {
                mapIndexed { index, char ->
                    if (index != lastIndex) {
                        AnimatedContent(
                            targetState = char,
                            transitionSpec = { slideInVertically { -it } togetherWith slideOutVertically { it } },
                            label = stowWatchIncrementAnimationLabel
                        ) { digit ->
                            StopWatchChar(
                                char = digit
                            )
                        }
                    } else {
                        StopWatchChar(
                            char = char
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StopWatchChar(
    char: Char,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        text = char.toString(),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

private fun Long.formatTime(): String {
    val minutes = this / 1.minutes.inWholeMilliseconds
    val seconds = (this % 1.minutes.inWholeMilliseconds) / 1.seconds.inWholeMilliseconds
    val millis = (this % 1.seconds.inWholeMilliseconds) / 100
    // Output should be "0:00,0"
    return String.format(Locale.current.platformLocale, stowWatchTimeFormat, minutes, seconds, millis)
}
