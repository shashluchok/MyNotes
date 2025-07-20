package com.shashluchok.medianotes.domain.notes.update

import com.shashluchok.medianotes.container.MediaNotesRepository
import com.shashluchok.medianotes.data.MediaNote

internal class UpdateMediaNoteInteractorImpl(
    private val mediaNotesRepository: MediaNotesRepository
) : UpdateMediaNoteInteractor {
    override suspend operator fun invoke(mediaNote: MediaNote) {
        mediaNotesRepository.update(mediaNote)
    }
}
