package com.shashluchok.medianotes.presentation.screen.medianotes

import kotlinx.collections.immutable.ImmutableList

internal sealed interface MediaNoteItem {
    val id: String
    val createdAt: String

    sealed interface EditableMediaNoteItem : MediaNoteItem

    data class Voice(
        override val id: String,
        override val createdAt: String,
        val path: String,
        val peaks: ImmutableList<Float>,
        val durationMillis: Long,
        val duration: String
    ) : MediaNoteItem

    data class Image(
        override val id: String,
        override val createdAt: String,
        val path: String,
        val text: String = ""
    ) : EditableMediaNoteItem

    data class Sketch(
        override val id: String,
        override val createdAt: String,
        val path: String
    ) : EditableMediaNoteItem

    data class Text(
        override val id: String,
        override val createdAt: String,
        val value: String
    ) : EditableMediaNoteItem
}
