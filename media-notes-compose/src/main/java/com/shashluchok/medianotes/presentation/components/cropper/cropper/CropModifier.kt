package com.shashluchok.medianotes.presentation.components.cropper.cropper

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.debugInspectorInfo
import com.shashluchok.medianotes.presentation.components.cropper.cropper.model.CropData
import com.shashluchok.medianotes.presentation.components.cropper.cropper.state.CropState
import com.shashluchok.medianotes.presentation.components.cropper.gesture.detectMotionEventsAsList
import com.shashluchok.medianotes.presentation.components.cropper.gesture.detectTransformGestures
import com.shashluchok.medianotes.presentation.utils.update
import kotlinx.coroutines.launch

internal fun Modifier.crop(
    cropState: CropState,
    onDown: ((CropData) -> Unit)? = null,
    onMove: ((CropData) -> Unit)? = null,
    onUp: ((CropData) -> Unit)? = null,
    onGestureStart: ((CropData) -> Unit)? = null,
    onGesture: ((CropData) -> Unit)? = null,
    onGestureEnd: ((CropData) -> Unit)? = null
) = composed(

    factory = {
        val coroutineScope = rememberCoroutineScope()

        val transformModifier = Modifier.pointerInput(Unit) {
            detectTransformGestures(
                consume = false,
                onGestureStart = {
                    onGestureStart?.invoke(cropState.cropData)
                },
                onGestureEnd = {
                    coroutineScope.launch {
                        cropState.onGestureEnd {
                            onGestureEnd?.invoke(cropState.cropData)
                        }
                    }
                },
                onGesture = { _, pan, zoom, _, mainPointer, _ ->
                    coroutineScope.launch {
                        cropState.onGesture(
                            panChange = pan,
                            zoomChange = zoom
                        )
                    }
                    onGesture?.invoke(cropState.cropData)
                    mainPointer.consume()
                }
            )
        }

        val touchModifier = Modifier.pointerInput(Unit) {
            detectMotionEventsAsList(
                onDown = {
                    coroutineScope.launch {
                        cropState.onDown(it)
                        onDown?.invoke(cropState.cropData)
                    }
                },
                onMove = {
                    coroutineScope.launch {
                        cropState.onMove(it)
                        onMove?.invoke(cropState.cropData)
                    }
                },
                onUp = {
                    coroutineScope.launch {
                        cropState.onUp()
                        onUp?.invoke(cropState.cropData)
                    }
                }
            )
        }

        val graphicsModifier = Modifier.graphicsLayer {
            this.update(cropState)
        }

        this.then(
            clipToBounds()
                .then(
                    if (cropState.isCropping) {
                        transformModifier.then(touchModifier)
                    } else {
                        Modifier
                    }
                )
                .then(graphicsModifier)
        )
    },
    inspectorInfo = debugInspectorInfo {
        name = "crop"
        properties["onDown"] = onGestureStart
        properties["onMove"] = onGesture
        properties["onUp"] = onGestureEnd
    }
)
