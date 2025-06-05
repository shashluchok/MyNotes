package com.shashluchok.medianotes.container

interface MediaNotesAppContainer {
    val appInfoProvider: AppInfoProvider
    val mediaNotesRepository: MediaNotesRepository
}
