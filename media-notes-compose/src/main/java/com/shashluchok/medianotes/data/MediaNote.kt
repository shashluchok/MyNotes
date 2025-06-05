package com.shashluchok.medianotes.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

sealed interface MediaNote {
    val id: String
    val updatedAt: Instant

    data class Voice(
        override val id: String = UUID.randomUUID().toString(),
        override val updatedAt: Instant = Clock.System.now(),
        val path: String
    ) : MediaNote

    data class Image(
        override val id: String = UUID.randomUUID().toString(),
        override val updatedAt: Instant = Clock.System.now(),
        val text: String = "",
        val path: String
    ) : MediaNote

    data class Sketch(
        override val id: String = UUID.randomUUID().toString(),
        override val updatedAt: Instant = Clock.System.now(),
        val path: String
    ) : MediaNote

    data class Text(
        override val id: String = UUID.randomUUID().toString(),
        override val updatedAt: Instant = Clock.System.now(),
        val value: String
    ) : MediaNote
}
