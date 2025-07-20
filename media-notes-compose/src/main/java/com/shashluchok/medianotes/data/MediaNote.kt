package com.shashluchok.medianotes.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

sealed interface MediaNote {
    val id: String
    val createdAt: Instant

    sealed interface WithText : MediaNote {
        val text: String
    }

    data class Voice(
        override val id: String = UUID.randomUUID().toString(),
        override val createdAt: Instant = Clock.System.now(),
        val path: String
    ) : MediaNote

    data class Image(
        override val id: String = UUID.randomUUID().toString(),
        override val createdAt: Instant = Clock.System.now(),
        override val text: String = "",
        val path: String
    ) : WithText

    data class Sketch(
        override val id: String = UUID.randomUUID().toString(),
        override val createdAt: Instant = Clock.System.now(),
        val path: String
    ) : MediaNote

    data class Text(
        override val id: String = UUID.randomUUID().toString(),
        override val createdAt: Instant = Clock.System.now(),
        override val text: String
    ) : WithText
}
