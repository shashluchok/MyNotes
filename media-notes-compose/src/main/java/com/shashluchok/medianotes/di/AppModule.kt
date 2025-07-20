package com.shashluchok.medianotes.di

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
import com.shashluchok.medianotes.presentation.screen.sketch.SketchViewModel
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

    // Viewmodels
    viewModel { CameraCaptureViewModel(androidContext().resources) }
    viewModel { GalleryImagePickerViewModel(get()) }
    viewModel {
        MediaNotesViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            androidContext().resources
        )
    }
    viewModel { ImageEditorViewModel(get(), get(), get(), get(), get(), androidContext().resources) }
    viewModel { SketchViewModel(get(), get(), androidContext().resources) }
}
