package com.shashluchok.medianotes.domain.notes.create

import com.shashluchok.medianotes.container.MediaNotesRepository
import com.shashluchok.medianotes.data.MediaNote

internal class CreateMediaNoteInteractorImpl(
    private val mediaNotesRepository: MediaNotesRepository
) : CreateMediaNoteInteractor {
    override suspend operator fun invoke(mediaNote: MediaNote) {
        mediaNotesRepository.create(mediaNote)
    }
}
