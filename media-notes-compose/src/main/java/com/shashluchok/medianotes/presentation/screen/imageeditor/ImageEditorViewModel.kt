package com.shashluchok.medianotes.presentation.screen.imageeditor

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewModelScope
import com.shashluchok.medianotes.R
import com.shashluchok.medianotes.data.MediaNote
import com.shashluchok.medianotes.domain.file.SaveBitmapToFileInteractor
import com.shashluchok.medianotes.domain.images.GetGalleryImagesInteractor
import com.shashluchok.medianotes.domain.notes.create.CreateMediaNoteInteractor
import com.shashluchok.medianotes.domain.notes.get.GetMediaNotesInteractor
import com.shashluchok.medianotes.presentation.MediaImage
import com.shashluchok.medianotes.presentation.components.cropper.cropper.crop.CropAgent
import com.shashluchok.medianotes.presentation.components.cropper.cropper.model.CropData
import com.shashluchok.medianotes.presentation.components.snackbar.SnackbarData
import com.shashluchok.medianotes.presentation.screen.AbsViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue

internal class ImageEditorViewModel(
    private val getGalleryImages: GetGalleryImagesInteractor,
    private val getMediaNotes: GetMediaNotesInteractor,
    private val createMediaNote: CreateMediaNoteInteractor,
    private val saveBitmapToFile: SaveBitmapToFileInteractor
) : AbsViewModel<ImageEditorViewModel.State>() {

    data class State(
        val images: ImmutableList<MediaImage> = persistentListOf(),
        val currentImageIndex: Int = 0,
        val caption: String = "",
        val isCropping: Boolean = false,
        val isLoading: Boolean = false,
        val isImageSaved: Boolean = false,
        val rotation: Float = 0F,
        val rotating: Boolean = false,
        val cropData: CropData? = null,
        val cropEnabled: Boolean = false,
        val snackbarData: SnackbarData? = null
    )

    private val cropAgent = CropAgent()

    override val mutableStateFlow: MutableStateFlow<State> = MutableStateFlow(State())

    fun onCropDataChange(cropData: CropData) {
        mutableStateFlow.update {
            it.copy(
                cropData = cropData
            )
        }
    }

    fun loadImages(context: Context, initialImage: MediaImage) {
        viewModelScope.launch {
            val images = when (initialImage.type) {
                MediaImage.Type.GALLERY -> getGalleryImages(context)
                MediaImage.Type.IMAGE_NOTE ->
                    getMediaNotes.mediaNotesFlow.value.filterIsInstance<MediaNote.Image>().map {
                        MediaImage(
                            id = it.id,
                            path = it.path,
                            type = MediaImage.Type.IMAGE_NOTE
                        )
                    }

                MediaImage.Type.SKETCH_NOTE ->
                    getMediaNotes.mediaNotesFlow.value.filterIsInstance<MediaNote.Sketch>()
                        .map {
                            MediaImage(
                                id = it.id,
                                path = it.path,
                                type = MediaImage.Type.SKETCH_NOTE
                            )
                        }
            }.toImmutableList()
            val currentImageIndex = images.indexOf(initialImage).takeIf { it >= 0 } ?: 0
            mutableStateFlow.update {
                it.copy(
                    images = images,
                    currentImageIndex = currentImageIndex,
                    cropEnabled = initialImage.type == MediaImage.Type.GALLERY
                )
            }
        }
    }

    fun onRotate() {
        mutableStateFlow.update {
            it.copy(
                rotation = it.rotation + ROTATION_INCREMENT
            )
        }
    }

    fun onImageSelected(index: Int) {
        mutableStateFlow.update {
            it.copy(
                currentImageIndex = index,
                caption = ""
            )
        }
    }

    fun onCaptionChange(caption: String) {
        mutableStateFlow.update {
            it.copy(caption = caption)
        }
    }

    fun onRotating(rotating: Boolean) {
        mutableStateFlow.update {
            it.copy(
                rotating = rotating
            )
        }
    }

    fun onCropChange(isCropping: Boolean) {
        mutableStateFlow.update {
            it.copy(
                isCropping = isCropping,
                rotation = 0f
            )
        }
    }

    suspend fun crop(
        onCropStart: (() -> Unit)? = null,
        minCropDurationMillis: Duration = Duration.ZERO,
        layoutDirection: LayoutDirection,
        density: Density
    ): ImageBitmap? {
        onCropStart?.invoke()
        val cropData = state.cropData ?: return null
        val result = measureTimedValue {
            cropAgent.crop(
                cropData = cropData,
                layoutDirection = layoutDirection,
                density = density
            )
        }
        if (result.duration < minCropDurationMillis) {
            delay(minCropDurationMillis - result.duration)
        }
        return result.value
    }

    fun onSendClick(
        context: Context,
        layoutDirection: LayoutDirection,
        density: Density
    ) {
        mutableStateFlow.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            val saveOperation = measureTimedValue {
                val bitmap = crop(
                    layoutDirection = layoutDirection,
                    density = density
                )
                bitmap?.let {
                    saveBitmapToFile.invoke(
                        context = context,
                        bitmap = bitmap
                    ).getOrNull()?.let { file ->
                        createMediaNote(
                            MediaNote.Image(
                                path = file.absolutePath,
                                text = state.caption
                            )
                        )
                    }
                }
                bitmap
            }

            if (saveOperation.duration < MIN_SAVE_DURATION) {
                delay(MIN_SAVE_DURATION - saveOperation.duration)
            }

            if (saveOperation.value != null) {
                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        isImageSaved = true
                    )
                }
            } else {
                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        isImageSaved = false,
                        snackbarData = SnackbarData(
                            titleResId = R.string.screen_image_editor__snackbar__save_error__title,
                            onDismiss = {
                                mutableStateFlow.update { it.copy(snackbarData = null) }
                            }
                        )
                    )
                }
            }
        }
    }

    companion object {
        private val MIN_SAVE_DURATION = 1.seconds
        private const val ROTATION_INCREMENT = 90f
    }
}
