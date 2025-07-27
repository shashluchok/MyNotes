package com.shashluchok.medianotes.presentation.screen.cameracapture

import com.shashluchok.medianotes.presentation.data.MediaImage
import com.shashluchok.medianotes.presentation.components.snackbar.SnackbarData

internal data class CameraCaptureState(
    val cameraState: CameraState = CameraState.NotActive,
    val switchCamerasEnabled: Boolean = false,
    val frontCameraOn: Boolean = false,
    val switchCameraEnabled: Boolean = false,
    val flashState: FlashState = FlashState.OFF,
    val snackbarData: SnackbarData? = null
) {
    enum class FlashState {
        OFF, ON, TORCH;

        fun next(): FlashState {
            val nextOrdinal = (this.ordinal + 1) % entries.size
            return entries[nextOrdinal]
        }
    }

    internal sealed class CameraState {

        data object NotActive : CameraState()

        data object Active : CameraState()

        data object Capturing : CameraState()

        data class PhotoCaptured(
            val mediaImage: MediaImage
        ) : CameraState()
    }
}
