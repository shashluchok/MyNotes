package com.shashluchok.medianotes.domain.notes.get

import com.shashluchok.medianotes.data.MediaNote
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow

internal interface GetMediaNotesInteractor {
    val mediaNotesFlow: StateFlow<ImmutableList<MediaNote>>
}
