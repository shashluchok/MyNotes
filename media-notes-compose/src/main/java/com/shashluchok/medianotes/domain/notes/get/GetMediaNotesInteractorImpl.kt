package com.shashluchok.medianotes.domain.notes.get

import com.shashluchok.medianotes.container.MediaNotesRepository
import com.shashluchok.medianotes.data.MediaNote
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow

internal class GetMediaNotesInteractorImpl(
    mediaNotesRepository: MediaNotesRepository
) : GetMediaNotesInteractor {
    override val mediaNotesFlow: StateFlow<ImmutableList<MediaNote>> = mediaNotesRepository.notesFlow
}
