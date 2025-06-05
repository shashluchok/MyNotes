package com.shashluchok.medianotes.sample.container

import com.shashluchok.medianotes.container.MediaNotesRepository
import com.shashluchok.medianotes.data.MediaNote
import com.shashluchok.medianotes.sample.data.db.medianote.DbMediaNote
import com.shashluchok.medianotes.sample.domain.db.MediaNotesDatabase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class MediaNotesRepositoryImpl(
    private val mediaNotesDatabase: MediaNotesDatabase
) : MediaNotesRepository {

    override val notesFlow: StateFlow<ImmutableList<MediaNote>> =
        mediaNotesDatabase.mediaNotesDao().getMediaNotesFlow().map {
            it.map { it.toMediaNote() }.filterNotNull().toImmutableList()
        }.stateIn(
            scope = MainScope(),
            started = SharingStarted.Eagerly,
            initialValue = persistentListOf()
        )

    override suspend fun getNoteById(noteId: String) = withContext(Dispatchers.IO) {
        mediaNotesDatabase.mediaNotesDao().getMediaNoteById(noteId)?.toMediaNote()
    }

    override suspend fun update(note: MediaNote) = withContext(Dispatchers.IO) {
        val mediaNote = getNoteById(note.id) ?: return@withContext
        mediaNotesDatabase.mediaNotesDao().updateMediaNote(mediaNote.toDbMediaNote())
    }

    override suspend fun delete(vararg noteId: String) = withContext(Dispatchers.IO) {
        mediaNotesDatabase.mediaNotesDao().deleteMediaNotes(noteId.asList())
    }

    override suspend fun create(mediaNote: MediaNote) = withContext(Dispatchers.IO) {
        mediaNotesDatabase.mediaNotesDao().insertMediaNote(mediaNote.toDbMediaNote())
    }

    private fun DbMediaNote.toMediaNote(): MediaNote? {
        return when (this.type) {
            DbMediaNote.Type.VOICE -> path?.let {
                MediaNote.Voice(
                    path = path
                )
            }

            DbMediaNote.Type.IMAGE -> path?.let {
                MediaNote.Image(
                    path = path,
                    text = text ?: ""
                )
            }

            DbMediaNote.Type.SKETCH -> path?.let {
                MediaNote.Sketch(
                    path = path
                )
            }

            DbMediaNote.Type.TEXT -> MediaNote.Text(
                value = text ?: ""
            )
        }
    }

    private fun MediaNote.toDbMediaNote(): DbMediaNote {
        return when (this) {
            is MediaNote.Image -> DbMediaNote(
                id = id,
                updatedAt = updatedAt,
                path = path,
                type = DbMediaNote.Type.IMAGE,
                text = text
            )

            is MediaNote.Sketch -> DbMediaNote(
                id = id,
                updatedAt = updatedAt,
                path = path,
                type = DbMediaNote.Type.SKETCH
            )

            is MediaNote.Text -> DbMediaNote(
                id = id,
                updatedAt = updatedAt,
                text = value,
                type = DbMediaNote.Type.TEXT
            )

            is MediaNote.Voice -> DbMediaNote(
                id = id,
                updatedAt = updatedAt,
                path = path,
                type = DbMediaNote.Type.VOICE
            )
        }
    }
}
