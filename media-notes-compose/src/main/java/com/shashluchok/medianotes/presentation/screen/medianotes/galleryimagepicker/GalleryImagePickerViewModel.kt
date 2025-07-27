package com.shashluchok.medianotes.presentation.screen.medianotes.galleryimagepicker

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.shashluchok.medianotes.domain.images.GetGalleryImagesInteractor
import com.shashluchok.medianotes.presentation.data.MediaImage
import com.shashluchok.medianotes.presentation.screen.AbsViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class GalleryImagePickerViewModel(
    private val getGalleryImages: GetGalleryImagesInteractor
) : AbsViewModel<GalleryImagePickerViewModel.State>() {

    data class State(
        val images: ImmutableList<MediaImage> = persistentListOf()
    )

    sealed interface Action {
        data class OnDisplay(val context: Context) : Action
    }

    override val mutableStateFlow: MutableStateFlow<State> = MutableStateFlow(State())

    fun onAction(action: Action) {
        when (action) {
            is Action.OnDisplay -> loadImages(action.context)
        }
    }

    private fun loadImages(context: Context) {
        viewModelScope.launch {
            state = state.copy(
                images = getGalleryImages(context)
            )
        }
    }
}
