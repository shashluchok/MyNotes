package com.shashluchok.medianotes.domain.notes.update

import com.shashluchok.medianotes.data.MediaNote

internal interface UpdateMediaNoteInteractor {
    suspend operator fun invoke(mediaNote: MediaNote)
}
