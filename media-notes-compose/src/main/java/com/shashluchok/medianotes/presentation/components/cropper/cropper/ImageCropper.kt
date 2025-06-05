package com.shashluchok.medianotes.presentation.components.cropper.cropper

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import com.shashluchok.medianotes.presentation.components.cropper.cropper.cropdefaults.CropDefaults
import com.shashluchok.medianotes.presentation.components.cropper.cropper.cropdefaults.CropStyle
import com.shashluchok.medianotes.presentation.components.cropper.cropper.draw.DrawingOverlay
import com.shashluchok.medianotes.presentation.components.cropper.cropper.draw.ImageDrawCanvas
import com.shashluchok.medianotes.presentation.components.cropper.cropper.state.CropState

private const val pressedStateBackgroundColorAlpha = 0.7f

@Composable
internal fun ImageCropper(
    cropState: CropState,
    isCropping: Boolean,
    onRotating: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    contentDescription: String? = null,
    cropStyle: CropStyle = CropDefaults.style(),
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality
) {
    val semantics = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }

    BoxWithConstraints(
        modifier = modifier
            .clipToBounds()
            .then(semantics),
        contentAlignment = Alignment.Center
    ) {
        LaunchedEffect(constraints) {
            cropState.initWithConstraints(constraints)
        }
        if (cropState.isInitialized) {
            LaunchedEffect(cropState) {
                snapshotFlow { cropState.isRotationRunning }.collect { isRunning ->
                    onRotating(isRunning)
                }
            }
            LaunchedEffect(isCropping) {
                cropState.setCropping(isCropping)
            }
            LaunchedEffect(rotation) {
                cropState.rotate(rotation)
            }
            ImageCropper(
                cropStyle = cropStyle,
                filterQuality = filterQuality,
                cropState = cropState
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.ImageCropper(
    cropStyle: CropStyle,
    filterQuality: FilterQuality,
    cropState: CropState,
    modifier: Modifier = Modifier
) {
    val containerSize = with(LocalDensity.current) {
        DpSize(
            constraints.maxWidth.toDp(),
            constraints.maxHeight.toDp()
        )
    }

    Box(
        modifier = Modifier.size(containerSize),
        contentAlignment = Alignment.Center
    ) {
        ImageDrawCanvas(
            modifier = modifier
                .crop(cropState)
                .fillMaxSize(),
            imageBitmap = cropState.imageBitmap,
            filterQuality = filterQuality
        )

        AnimatedVisibility(
            visible = cropState.isCropping,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val isHandleTouched by remember(cropState) {
                derivedStateOf {
                    handlesTouched(cropState.touchRegion)
                }
            }

            val pressedStateColor = remember(cropStyle.backgroundColor) {
                cropStyle.backgroundColor
                    .copy(cropStyle.backgroundColor.alpha * pressedStateBackgroundColorAlpha)
            }

            val transparentColor by animateColorAsState(
                animationSpec = tween(easing = LinearEasing),
                targetValue = if (isHandleTouched) pressedStateColor else cropStyle.backgroundColor
            )

            DrawingOverlay(
                modifier = Modifier.fillMaxSize(),
                rect = cropState.overlayRect,
                cropShape = cropState.cropProperties.cropShape,
                drawGrid = cropStyle.drawGrid,
                overlayColor = cropStyle.overlayColor,
                handleColor = cropStyle.handleColor,
                strokeWidth = cropStyle.strokeWidth,
                handleSize = cropState.cropProperties.handleSize,
                transparentColor = transparentColor
            )
        }
    }
}
