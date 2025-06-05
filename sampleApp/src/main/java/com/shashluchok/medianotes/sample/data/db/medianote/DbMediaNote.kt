package com.shashluchok.medianotes.sample.data.db.medianote

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(tableName = "mediaNotes")
data class DbMediaNote(
    @PrimaryKey val id: String,
    val updatedAt: Instant,
    val path: String? = null,
    val text: String? = null,
    val type: Type
) {
    enum class Type { VOICE, IMAGE, SKETCH, TEXT }
}
