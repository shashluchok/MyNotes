package com.shashluchok.medianotes.di

import android.content.res.Resources
import com.shashluchok.audiorecorder.audio.AudioPlayer
import com.shashluchok.audiorecorder.audio.AudioPlayerImpl
import com.shashluchok.medianotes.container.AppInfoProvider
import com.shashluchok.medianotes.container.MediaNotesAppContainer
import com.shashluchok.medianotes.container.MediaNotesRepository
import com.shashluchok.medianotes.domain.file.SaveBitmapToFileInteractor
import com.shashluchok.medianotes.domain.file.SaveBitmapToFileInteractorImpl
import com.shashluchok.medianotes.domain.images.GetGalleryImagesInteractor
import com.shashluchok.medianotes.domain.images.GetGalleryImagesInteractorImpl
import com.shashluchok.medianotes.domain.notes.create.CreateMediaNoteInteractor
import com.shashluchok.medianotes.domain.notes.create.CreateMediaNoteInteractorImpl
import com.shashluchok.medianotes.domain.notes.delete.DeleteMediaNotesInteractor
import com.shashluchok.medianotes.domain.notes.delete.DeleteMediaNotesInteractorImpl
import com.shashluchok.medianotes.domain.notes.get.GetMediaNotesInteractor
import com.shashluchok.medianotes.domain.notes.get.GetMediaNotesInteractorImpl
import com.shashluchok.medianotes.domain.notes.update.UpdateMediaNoteInteractor
import com.shashluchok.medianotes.domain.notes.update.UpdateMediaNoteInteractorImpl
import com.shashluchok.medianotes.domain.settings.OpenSettingsInteractor
import com.shashluchok.medianotes.domain.settings.OpenSettingsInteractorImpl
import com.shashluchok.medianotes.presentation.screen.cameracapture.CameraCaptureViewModel
import com.shashluchok.medianotes.presentation.screen.imageeditor.ImageEditorViewModel
import com.shashluchok.medianotes.presentation.screen.medianotes.MediaNotesViewModel
import com.shashluchok.medianotes.presentation.screen.medianotes.galleryimagepicker.GalleryImagePickerViewModel
import com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist.MediaNotesListViewModel
import com.shashluchok.medianotes.presentation.screen.sketch.SketchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal fun appModule(
    mediaNotesAppContainer: MediaNotesAppContainer
) = module {
    // Container
    single<AppInfoProvider> { mediaNotesAppContainer.appInfoProvider }

    // Repositories
    single<MediaNotesRepository> { mediaNotesAppContainer.mediaNotesRepository }

    // Interactors
    single<GetGalleryImagesInteractor> { GetGalleryImagesInteractorImpl() }
    single<SaveBitmapToFileInteractor> { SaveBitmapToFileInteractorImpl() }
    single<CreateMediaNoteInteractor> { CreateMediaNoteInteractorImpl(get()) }
    single<UpdateMediaNoteInteractor> { UpdateMediaNoteInteractorImpl(get()) }
    single<DeleteMediaNotesInteractor> { DeleteMediaNotesInteractorImpl(get()) }
    single<GetMediaNotesInteractor> { GetMediaNotesInteractorImpl(get()) }
    single<OpenSettingsInteractor> { OpenSettingsInteractorImpl() }

    // Components
    single<CoroutineScope> { CoroutineScope(Dispatchers.Default) }
    single<AudioPlayer> { AudioPlayerImpl(get()) }
    single<Resources> { androidContext().resources }

    // Viewmodels
    viewModel { CameraCaptureViewModel(get()) }
    viewModel { GalleryImagePickerViewModel(get()) }
    viewModel {
        MediaNotesViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        ImageEditorViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel { SketchViewModel(get(), get(), get()) }
    viewModel { MediaNotesListViewModel(get(), get(), get()) }
}
