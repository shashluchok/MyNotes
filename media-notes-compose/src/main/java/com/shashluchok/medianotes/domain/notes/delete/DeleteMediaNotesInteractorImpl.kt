package com.shashluchok.medianotes.domain.notes.delete

import com.shashluchok.medianotes.container.MediaNotesRepository
import com.shashluchok.medianotes.data.MediaNote
import kotlinx.coroutines.flow.filter
import java.io.File

internal class DeleteMediaNotesInteractorImpl(
    private val mediaNotesRepository: MediaNotesRepository
) : DeleteMediaNotesInteractor {
    override suspend operator fun invoke(vararg noteIds: String) {
        mediaNotesRepository.notesFlow.value
            .filter { noteIds.contains(it.id) }
            .filterIsInstance<MediaNote.WithFile>()
            .onEach {
                File(it.path).delete()
            }
        mediaNotesRepository.delete(*noteIds)
    }
}
