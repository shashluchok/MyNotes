package com.shashluchok.medianotes.presentation.screen.cameracapture

import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.os.Environment
import android.provider.MediaStore
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import androidx.camera.core.ImageCapture.FLASH_MODE_ON
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.shashluchok.medianotes.R
import com.shashluchok.medianotes.presentation.MediaImage
import com.shashluchok.medianotes.presentation.components.snackbar.SnackbarData
import com.shashluchok.medianotes.presentation.screen.AbsViewModel
import com.shashluchok.medianotes.presentation.screen.cameracapture.CameraCaptureState.CameraState
import com.shashluchok.medianotes.presentation.screen.cameracapture.CameraCaptureState.FlashState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

internal class CameraCaptureViewModel(
    private val resources: Resources
) : AbsViewModel<CameraCaptureState>() {

    sealed interface Action {
        data class BindPreview(
            val lifecycleOwner: LifecycleOwner,
            val previewView: PreviewView,
            val context: Context
        ) : Action

        data object ChangeFlashModeClick : Action
        data object SwitchCameraClick : Action
        data class CapturePhotoClick(
            val context: Context
        ) : Action

        data class NewScale(val scale: Float) : Action
        data class NewFocus(val meteringPoint: MeteringPoint) : Action
        data object OnCaptureHandled : Action
    }

    override val mutableStateFlow: MutableStateFlow<CameraCaptureState> = MutableStateFlow(
        CameraCaptureState()
    )

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private val imageCapture = ImageCapture.Builder().build()
    private val preview = Preview.Builder().build()

    fun onAction(action: Action) {
        when (action) {
            is Action.BindPreview -> bindPreview(
                action.lifecycleOwner,
                action.previewView,
                action.context
            )

            is Action.CapturePhotoClick -> capturePhoto(action.context)
            Action.ChangeFlashModeClick -> changeFlashMode()
            is Action.NewFocus -> focus(action.meteringPoint)
            is Action.NewScale -> scale(action.scale)
            Action.SwitchCameraClick -> switchCamera()
            Action.OnCaptureHandled -> onCaptureHandled()
        }
    }

    private fun bindPreview(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        context: Context
    ) {
        viewModelScope.launch {
            cameraProvider = ProcessCameraProvider.getInstance(context).get().also { cameraProvider ->

                val cameraSelector = getCameraSelector(state.frontCameraOn)

                val useCaseGroup = UseCaseGroup.Builder()
                    .apply {
                        previewView.viewPort?.let(::setViewPort)
                    }
                    .addUseCase(imageCapture)
                    .addUseCase(preview)
                    .build()

                cameraProvider.unbindAll()

                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    useCaseGroup
                )

                preview.surfaceProvider = previewView.surfaceProvider

                val switchCamerasEnabled = listOf(
                    DEFAULT_FRONT_CAMERA,
                    DEFAULT_FRONT_CAMERA
                ).filter(cameraProvider::hasCamera).size > 1

                mutableStateFlow.update {
                    it.copy(
                        cameraState = CameraState.Active,
                        switchCamerasEnabled = switchCamerasEnabled
                    )
                }
            }
        }
    }

    private fun switchCamera() {
        if (state.cameraState == CameraState.Active) {
            val nextCameraSelector = getCameraSelector(
                state.frontCameraOn.not()
            )

            if (cameraProvider?.hasCamera(nextCameraSelector) == false) return

            mutableStateFlow.update {
                it.copy(
                    frontCameraOn = nextCameraSelector == DEFAULT_FRONT_CAMERA,
                    cameraState = CameraState.NotActive
                )
            }

            preview.surfaceProvider = null
        }
    }

    private fun capturePhoto(
        context: Context
    ) {
        mutableStateFlow.update {
            it.copy(
                cameraState = CameraState.Capturing
            )
        }

        val operationStartTime = Clock.System.now()

        suspend fun delayTimeToMinDurationIfNeeded() {
            val operationTime = Clock.System.now() - operationStartTime
            val diff = IMAGE_CAPTURE_MIN_DURATION - operationTime
            if (diff.isPositive()) delay(diff)
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, generateOutputFileName())
            put(MediaStore.MediaColumns.MIME_TYPE, OUTPUT_PHOTO_MIME_TYPE)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        }

        val metadata = ImageCapture.Metadata().apply {
            // Mirror image when using the front camera
            isReversedHorizontal = state.frontCameraOn
        }

        val outputFileOptions =
            ImageCapture.OutputFileOptions
                .Builder(
                    context.contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                .setMetadata(metadata)
                .build()

        imageCapture.takePicture(
            outputFileOptions,
            Dispatchers.Default.asExecutor(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    viewModelScope.launch {
                        delayTimeToMinDurationIfNeeded()

                        val fileId = outputFileResults.savedUri?.lastPathSegment ?: run {
                            onImageSaveError()
                            return@launch
                        }
                        val filePath = outputFileResults.savedUri?.path ?: run {
                            onImageSaveError()
                            return@launch
                        }

                        mutableStateFlow.update {
                            it.copy(
                                cameraState = CameraState.PhotoCaptured(
                                    MediaImage(
                                        id = fileId,
                                        type = MediaImage.Type.GALLERY,
                                        path = filePath
                                    )
                                )
                            )
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    viewModelScope.launch {
                        delayTimeToMinDurationIfNeeded()
                        onImageSaveError()
                    }
                }
            }
        )
    }

    private fun changeFlashMode() {
        if (camera?.cameraInfo?.hasFlashUnit() == false) return
        val newState = state.flashState.next()
        when (newState) {
            FlashState.ON -> {
                camera?.cameraControl?.enableTorch(false)
                imageCapture.flashMode = FLASH_MODE_ON
            }

            FlashState.OFF -> {
                camera?.cameraControl?.enableTorch(false)
                imageCapture.flashMode = FLASH_MODE_OFF
            }

            FlashState.TORCH -> {
                imageCapture.flashMode = FLASH_MODE_OFF
                camera?.cameraControl?.enableTorch(true)
            }
        }

        mutableStateFlow.update {
            it.copy(
                flashState = newState
            )
        }
    }

    private fun focus(meteringPoint: MeteringPoint) {
        val camera = camera ?: return

        val meteringAction = FocusMeteringAction.Builder(meteringPoint).build()
        camera.cameraControl.startFocusAndMetering(meteringAction)
    }

    private fun scale(scaleFactor: Float) {
        val camera = camera ?: return
        val currentZoomRatio: Float = camera.cameraInfo.zoomState.value?.zoomRatio ?: 1f
        camera.cameraControl.setZoomRatio(
            (scaleFactor * currentZoomRatio).coerceAtMost(MAX_ZOOM)
        )
    }

    private fun onSnackbarDismiss() {
        mutableStateFlow.update {
            it.copy(
                snackbarData = null
            )
        }
    }

    private fun onCaptureHandled() {
        mutableStateFlow.update {
            it.copy(cameraState = CameraState.NotActive)
        }
    }

    private fun getCameraSelector(frontCamera: Boolean) =
        if (frontCamera) {
            DEFAULT_FRONT_CAMERA
        } else {
            DEFAULT_BACK_CAMERA
        }

    private fun onImageSaveError() {
        mutableStateFlow.update {
            it.copy(
                cameraState = CameraState.Active,
                snackbarData = SnackbarData(
                    title = resources.getString(R.string.screen_camera_capture__snackbar__error__title),
                    onDismiss = ::onSnackbarDismiss
                )
            )
        }
    }

    private fun generateOutputFileName(): String = "${UUID.randomUUID()}.$OUTPUT_PHOTO_EXTENSION"

    companion object {
        private val IMAGE_CAPTURE_MIN_DURATION = 1.seconds
        private const val MAX_ZOOM = 3f
        private const val OUTPUT_PHOTO_EXTENSION = ".jpg"
        private const val OUTPUT_PHOTO_MIME_TYPE = "image/jpeg"
    }
}
