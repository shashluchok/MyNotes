package com.shashluchok.medianotes.presentation

import kotlinx.serialization.Serializable

@Serializable
internal data class MediaImage(
    val id: String,
    val path: String,
    val type: Type,
    val text: String = ""
) {
    enum class Type {
        GALLERY, IMAGE_NOTE, SKETCH_NOTE
    }
}
