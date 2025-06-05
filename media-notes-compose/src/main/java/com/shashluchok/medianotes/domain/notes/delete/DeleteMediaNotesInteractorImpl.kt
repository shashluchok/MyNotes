package com.shashluchok.medianotes.domain.notes.delete

import com.shashluchok.medianotes.container.MediaNotesRepository

internal class DeleteMediaNotesInteractorImpl(
    private val mediaNotesRepository: MediaNotesRepository
) : DeleteMediaNotesInteractor {
    override suspend operator fun invoke(vararg noteIds: String) {
        mediaNotesRepository.delete(*noteIds)
    }
}
