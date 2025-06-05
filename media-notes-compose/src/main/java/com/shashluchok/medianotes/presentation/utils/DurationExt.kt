package com.shashluchok.medianotes.presentation.utils

import java.util.Locale
import kotlin.time.Duration

private const val AUDIO_DURATION_STRING_FORMAT = "%02d:%02d"
private const val SECONDS_IN_MINUTE = 60

internal fun Duration.toAudioDisplayString(): String {
    val totalSeconds = inWholeSeconds
    val minutes = totalSeconds / SECONDS_IN_MINUTE
    val seconds = totalSeconds % SECONDS_IN_MINUTE
    return String.format(Locale.getDefault(), AUDIO_DURATION_STRING_FORMAT, minutes, seconds)
}
