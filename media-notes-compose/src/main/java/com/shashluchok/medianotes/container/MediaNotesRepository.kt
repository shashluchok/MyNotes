package com.shashluchok.medianotes.container

import com.shashluchok.medianotes.data.MediaNote
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow

interface MediaNotesRepository {
    val notesFlow: StateFlow<ImmutableList<MediaNote>>
    suspend fun getNoteById(noteId: String): MediaNote?
    suspend fun update(mediaNote: MediaNote)
    suspend fun delete(vararg noteId: String)
    suspend fun create(mediaNote: MediaNote)
}
