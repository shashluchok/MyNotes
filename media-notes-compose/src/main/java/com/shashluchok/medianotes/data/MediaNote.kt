package com.shashluchok.medianotes.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

sealed class MediaNote {
    val id: String = UUID.randomUUID().toString()
    val updatedAt: Instant = Clock.System.now()

    data class Voice(
        val path: String
    ) : MediaNote()

    data class Image(
        val text: String = "",
        val path: String
    ) : MediaNote()

    data class Sketch(
        val path: String
    ) : MediaNote()

    data class Text(
        val value: String
    ) : MediaNote()
}
