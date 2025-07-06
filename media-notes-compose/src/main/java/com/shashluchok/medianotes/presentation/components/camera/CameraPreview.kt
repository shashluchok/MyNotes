package com.shashluchok.medianotes.presentation.components.camera

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
internal fun CameraPreview(
    previewView: PreviewView,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

@Composable
internal fun rememberPreviewView(): PreviewView {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    return remember {
        val cameraController = LifecycleCameraController(context)
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            controller = cameraController
            cameraController.bindToLifecycle(lifecycleOwner)
        }
    }
}
