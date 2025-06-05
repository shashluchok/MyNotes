package com.shashluchok.medianotes.presentation

import com.shashluchok.medianotes.presentation.screen.medianotes.MediaNoteItem

internal object SharedTransitionUtils {

    sealed class SharedElementType {
        data object Camera : SharedElementType()
        data class Image(val image: MediaImage) : SharedElementType()
        data class MediaNote(val noteItem: MediaNoteItem) : SharedElementType()
    }

    fun getKeyByElementType(elementType: SharedElementType): String {
        return when (elementType) {
            SharedElementType.Camera -> elementType.toString()
            is SharedElementType.Image -> elementType.image.id
            is SharedElementType.MediaNote -> elementType.noteItem.id
        }
    }
}
