package com.shashluchok.medianotes.presentation.screen.imageeditor

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewModelScope
import com.shashluchok.medianotes.R
import com.shashluchok.medianotes.data.MediaNote
import com.shashluchok.medianotes.domain.file.SaveBitmapToFileInteractor
import com.shashluchok.medianotes.domain.images.GetGalleryImagesInteractor
import com.shashluchok.medianotes.domain.notes.create.CreateMediaNoteInteractor
import com.shashluchok.medianotes.domain.notes.delete.DeleteMediaNotesInteractor
import com.shashluchok.medianotes.domain.notes.get.GetMediaNotesInteractor
import com.shashluchok.medianotes.presentation.data.MediaImage
import com.shashluchok.medianotes.presentation.components.cropper.cropper.crop.CropAgent
import com.shashluchok.medianotes.presentation.components.cropper.cropper.model.CropData
import com.shashluchok.medianotes.presentation.components.snackbar.SnackbarData
import com.shashluchok.medianotes.presentation.data.ActionButton
import com.shashluchok.medianotes.presentation.data.AlertDialogData
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
    private val saveBitmapToFile: SaveBitmapToFileInteractor,
    private val deleteMediaNote: DeleteMediaNotesInteractor,
    private val resources: Resources
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
        val deleteEnabled: Boolean = false,
        val snackbarData: SnackbarData? = null,
        val shouldDismiss: Boolean = false,
        val alertDialogData: AlertDialogData? = null
    )

    sealed interface Action {
        data class OnCropDataChange(val cropData: CropData) : Action
        data class OnDisplay(val context: Context, val initialImage: MediaImage) : Action
        data object OnRotateClick : Action
        data class OnImageSelected(val imageIndex: Int) : Action
        data class OnCaptionChange(val caption: String) : Action
        data class OnRotating(val isRotating: Boolean) : Action
        data class OnCroppingChange(val isCropping: Boolean) : Action

        data class OnSendClick(
            val context: Context,
            val layoutDirection: LayoutDirection,
            val density: Density
        ) : Action

        data object OnDeleteMediaNoteClick : Action
    }

    private val cropAgent = CropAgent()

    override val mutableStateFlow: MutableStateFlow<State> = MutableStateFlow(State())

    fun onAction(action: Action) {
        when (action) {
            is Action.OnCaptionChange -> onCaptionChange(action.caption)
            is Action.OnCropDataChange -> onCropDataChange(action.cropData)
            is Action.OnCroppingChange -> onCropChange(action.isCropping)
            is Action.OnDisplay -> loadImages(action.context, action.initialImage)
            is Action.OnImageSelected -> onImageSelected(action.imageIndex)
            Action.OnRotateClick -> onRotate()
            is Action.OnRotating -> onRotating(action.isRotating)
            is Action.OnSendClick -> onSendClick(
                action.context,
                action.layoutDirection,
                action.density
            )

            Action.OnDeleteMediaNoteClick -> onDeleteMediaNoteClick()
        }
    }

    private fun onDeleteMediaNoteClick() {
        val noteToDelete = getMediaNotes.mediaNotesFlow.value.firstOrNull {
            it.id == state.images.getOrNull(state.currentImageIndex)?.id
        } ?: return

        showNoteDeleteAlert(noteToDelete)
    }

    private fun showNoteDeleteAlert(noteToDelete: MediaNote) {
        mutableStateFlow.update {
            it.copy(
                alertDialogData = AlertDialogData(
                    onDismiss = ::onDismissDialog,
                    title = resources.getString(R.string.screen_image_editor__dialog__note_delete__title),
                    confirmButton = ActionButton(
                        title = resources.getString(
                            R.string.screen_image_editor__dialog__note_delete__confirm_button
                        ),
                        onClick = {
                            viewModelScope.launch {
                                deleteMediaNote(noteToDelete.id)
                                onDismissDialog()
                            }
                        }
                    ),
                    message = resources.getString(R.string.screen_image_editor__dialog__note_delete__message),
                    dismissButton = ActionButton(
                        title = resources.getString(
                            R.string.screen_image_editor__dialog__note_delete__cancel_button
                        ),
                        onClick = ::onDismissDialog
                    )
                )
            )
        }
    }

    private fun onDismissDialog() {
        mutableStateFlow.update {
            it.copy(
                alertDialogData = null
            )
        }
    }

    private fun onCropDataChange(cropData: CropData) {
        mutableStateFlow.update {
            it.copy(
                cropData = cropData
            )
        }
    }

    private fun loadImages(context: Context, initialImage: MediaImage) {
        viewModelScope.launch {
            when (initialImage.type) {
                MediaImage.Type.GALLERY -> {
                    val images = getGalleryImages(context)
                    updateState(
                        images = images,
                        initialImage = initialImage,
                        cropEnabled = true
                    )
                }

                MediaImage.Type.IMAGE_NOTE -> getMediaNotes.mediaNotesFlow.collect { notes ->
                    val images = notes.filterIsInstance<MediaNote.Image>().map {
                        MediaImage(
                            id = it.id,
                            path = it.path,
                            text = it.text,
                            type = MediaImage.Type.IMAGE_NOTE
                        )
                    }.toImmutableList()

                    updateState(
                        images = images,
                        initialImage = initialImage,
                        cropEnabled = false
                    )
                }

                MediaImage.Type.SKETCH_NOTE -> getMediaNotes.mediaNotesFlow.collect { notes ->
                    val images = notes.filterIsInstance<MediaNote.Sketch>()
                        .map {
                            MediaImage(
                                id = it.id,
                                path = it.path,
                                type = MediaImage.Type.SKETCH_NOTE
                            )
                        }.toImmutableList()

                    updateState(
                        images = images,
                        initialImage = initialImage,
                        cropEnabled = false
                    )
                }
            }
        }
    }

    private fun updateState(
        images: ImmutableList<MediaImage>,
        initialImage: MediaImage,
        cropEnabled: Boolean
    ) {
        val currentImageIndex = if (state.currentImageIndex == 0) {
            images.map { it.id }
                .indexOf(initialImage.id).takeIf { it >= 0 } ?: 0
        } else if (images.lastIndex < state.currentImageIndex) {
            state.currentImageIndex - 1
        } else {
            state.currentImageIndex
        }

        mutableStateFlow.update {
            it.copy(
                images = images,
                currentImageIndex = currentImageIndex,
                cropEnabled = cropEnabled,
                deleteEnabled = !cropEnabled,
                shouldDismiss = images.isEmpty()
            )
        }
    }

    private fun onRotate() {
        mutableStateFlow.update {
            it.copy(
                rotation = it.rotation + ROTATION_INCREMENT
            )
        }
    }

    private fun onImageSelected(index: Int) {
        mutableStateFlow.update {
            it.copy(
                currentImageIndex = index,
                caption = ""
            )
        }
    }

    private fun onCaptionChange(caption: String) {
        mutableStateFlow.update {
            it.copy(caption = caption)
        }
    }

    private fun onRotating(rotating: Boolean) {
        mutableStateFlow.update {
            it.copy(
                rotating = rotating
            )
        }
    }

    private fun onCropChange(isCropping: Boolean) {
        mutableStateFlow.update {
            it.copy(
                isCropping = isCropping,
                rotation = 0f
            )
        }
    }

    private fun onSendClick(
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
                            title = resources.getString(R.string.screen_image_editor__snackbar__save_error__title),
                            onDismiss = {
                                mutableStateFlow.update { it.copy(snackbarData = null) }
                            }
                        )
                    )
                }
            }
        }
    }

    private suspend fun crop(
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

    companion object {
        private val MIN_SAVE_DURATION = 1.seconds
        private const val ROTATION_INCREMENT = 90f
    }
}
