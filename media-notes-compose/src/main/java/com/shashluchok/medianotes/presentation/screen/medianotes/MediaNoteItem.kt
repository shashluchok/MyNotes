package com.shashluchok.medianotes.presentation.screen.medianotes

import kotlinx.collections.immutable.ImmutableList

internal sealed interface MediaNoteItem {

    data class CreatedTimeStamp(
        val hourAndMinute: String,
        val dayAndMonth: String
    )

    val id: String
    val createdTimeStamp: CreatedTimeStamp

    sealed interface EditableMediaNoteItem : MediaNoteItem

    data class Voice(
        override val id: String,
        override val createdTimeStamp: CreatedTimeStamp,
        val path: String,
        val peaks: ImmutableList<Float>,
        val durationMillis: Long,
        val duration: String
    ) : MediaNoteItem

    data class Image(
        override val id: String,
        override val createdTimeStamp: CreatedTimeStamp,
        val path: String,
        val text: String = ""
    ) : EditableMediaNoteItem

    data class Sketch(
        override val id: String,
        override val createdTimeStamp: CreatedTimeStamp,
        val path: String
    ) : EditableMediaNoteItem

    data class Text(
        override val id: String,
        override val createdTimeStamp: CreatedTimeStamp,
        val value: String
    ) : EditableMediaNoteItem
}
