package com.shashluchok.medianotes.domain.notes.create

import com.shashluchok.medianotes.data.MediaNote

internal interface CreateMediaNoteInteractor {
    suspend operator fun invoke(mediaNote: MediaNote)
}
