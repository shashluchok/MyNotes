package com.shashluchok.medianotes.domain.notes.delete

internal interface DeleteMediaNotesInteractor {
    suspend operator fun invoke(vararg noteIds: String)
}
