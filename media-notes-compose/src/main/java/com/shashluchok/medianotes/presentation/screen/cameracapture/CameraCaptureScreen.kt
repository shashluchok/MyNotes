package com.shashluchok.medianotes.presentation.screen.cameracapture

import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shashluchok.medianotes.presentation.MediaImage
import com.shashluchok.medianotes.presentation.SharedTransitionUtils
import com.shashluchok.medianotes.presentation.components.CustomRippleColor
import com.shashluchok.medianotes.presentation.components.LocalNavAnimatedVisibilityScope
import com.shashluchok.medianotes.presentation.components.LocalSharedTransitionScope
import com.shashluchok.medianotes.presentation.components.camera.BlurImage
import com.shashluchok.medianotes.presentation.components.camera.CameraPreview
import com.shashluchok.medianotes.presentation.components.dialog.LoadingAnimationDialog
import com.shashluchok.medianotes.presentation.components.mediaicon.MediaIconButton
import com.shashluchok.medianotes.presentation.components.snackbar.SnackbarHost
import com.shashluchok.medianotes.presentation.screen.cameracapture.CameraCaptureState.CameraState
import com.shashluchok.medianotes.presentation.screen.cameracapture.CameraCaptureState.FlashState
import kotlinx.coroutines.flow.first
import org.koin.androidx.compose.koinViewModel

private val preRotationAnimationSpec = tween<Float>(
    durationMillis = 150,
    delayMillis = 100,
    easing = LinearEasing
)
private const val preRotationAnimationInitialValue = 0f
private const val preRotationAnimationTargetValue = 90f
private val rotationAnimationSpec = tween<Float>(
    durationMillis = 75,
    easing = LinearEasing
)
private const val rotationAnimationInitialValue = 270f
private const val rotationAnimationTargetValue = 360f

private const val rotationVisibilityAnimationDuration = 75
private const val rotationCameraDistanceMultiplier = 16

private const val focusCircleSizePx = 50
private const val focusCircleSizeIncrementPx = 30
private const val focusCircleWidthPx = 5f

private val cameraControlsHeight = 64.dp
private val cameraControlsHorizontalArrangement = 48.dp
private val cameraControlsPadding = PaddingValues(bottom = 24.dp)
private val cameraCaptureButtonBorderWidth = 4.dp

private const val cameraCaptureFlashDuration = 500

@Composable
internal fun CameraCaptureScreen(
    onPhotoSaved: (MediaImage) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CameraCaptureViewModel = koinViewModel()
) {
    val state = viewModel.stateFlow.collectAsState().value

    LaunchedEffect(state.cameraState) {
        (state.cameraState as? CameraState.PhotoCaptured)?.mediaImage?.let {
            onPhotoSaved(it)
            viewModel.onAction(CameraCaptureViewModel.Action.OnCaptureHandled)
        }
    }

    CameraCaptureScreen(
        modifier = modifier.fillMaxSize(),
        cameraState = state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun CameraCaptureScreen(
    onAction: (CameraCaptureViewModel.Action) -> Unit,
    cameraState: CameraCaptureState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    val navAnimationTransition = LocalNavAnimatedVisibilityScope.current.transition

    LaunchedEffect(cameraState.frontCameraOn) {
        snapshotFlow { navAnimationTransition.isRunning }.first { it.not() }
        onAction(CameraCaptureViewModel.Action.BindPreview(lifecycleOwner, previewView, context))
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            val snackBarHostState = remember { SnackbarHostState() }
            SnackbarHost(
                snackbarData = cameraState.snackbarData,
                snackBarHostState = snackBarHostState,
                modifier = Modifier.padding(bottom = cameraControlsHeight)
            )
        }
    ) { padding ->
        with(LocalSharedTransitionScope.current) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .background(Color.Black),
                contentAlignment = Alignment.BottomCenter
            ) {
                FocusingBox(
                    onNewFocus = { offset ->
                        val meteringPointFactory = previewView.meteringPointFactory
                        val focusPoint = meteringPointFactory.createPoint(offset.x, offset.y)
                        onAction(CameraCaptureViewModel.Action.NewFocus(focusPoint))
                    },
                    enabled = cameraState.cameraState == CameraState.Active
                ) {
                    CameraPreview(
                        modifier = Modifier
                            .sharedElement(
                                state = rememberSharedContentState(
                                    key = SharedTransitionUtils.getKeyByElementType(
                                        SharedTransitionUtils.SharedElementType.Camera
                                    )
                                ),
                                animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
                            )
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, _, zoom, _ ->
                                    onAction(CameraCaptureViewModel.Action.NewScale(zoom))
                                }
                            },
                        previewView = previewView
                    )
                }

                RotatingBox(
                    modifier = Modifier.background(Color.Black),
                    key = cameraState.frontCameraOn,
                    animateVisibilityDuringRotation = true
                ) {
                    previewView.bitmap?.let {
                        BlurImage(
                            modifier = Modifier.fillMaxSize(),
                            bitmap = it
                        )
                    }
                }

                AnimatedVisibility(
                    modifier = Modifier
                        .align(Alignment.BottomCenter),
                    visible =
                    cameraState.cameraState == CameraState.Active,
                    enter = slideInVertically(tween()) { it },
                    exit = slideOutVertically(tween()) { it }
                ) {
                    CameraControls(
                        frontCameraOn = cameraState.frontCameraOn,
                        switchCamera = {
                            onAction(CameraCaptureViewModel.Action.SwitchCameraClick)
                        },
                        capturePhoto = {
                            onAction(CameraCaptureViewModel.Action.CapturePhotoClick(context))
                        },
                        onFlashModeClick = {
                            onAction(CameraCaptureViewModel.Action.ChangeFlashModeClick)
                        },
                        flashState = cameraState.flashState
                    )
                }
            }
        }
        if (cameraState.cameraState is CameraState.NotActive || cameraState.cameraState is CameraState.Capturing) {
            LoadingAnimationDialog()
        }

        CameraCaptureFlashBox(
            visible = cameraState.cameraState is CameraState.Capturing,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun CameraCaptureFlashBox(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            alpha.snapTo(1f)
            alpha.animateTo(0f, tween(durationMillis = cameraCaptureFlashDuration))
        }
    }

    Box(
       modifier = modifier.background(Color.White.copy(alpha = alpha.value))
    )
}

@Composable
private fun FocusingBox(
    onNewFocus: (Offset) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var focus by remember {
        mutableStateOf<Offset?>(null)
    }

    var focusProgress by remember {
        mutableFloatStateOf(1f)
    }

    LaunchedEffect(focus) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween()
        ) { value, _ ->
            focusProgress = value
        }
    }

    Box(
        modifier = modifier
            .drawWithContent {
                drawContent()
                focus?.let {
                    drawCircle(
                        color = Color.White.copy(alpha = 1 - focusProgress),
                        center = it,
                        radius = focusCircleSizePx + focusCircleSizeIncrementPx -
                            focusProgress * focusCircleSizeIncrementPx,
                        style = Stroke(width = focusCircleWidthPx)
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (focusProgress == 1f && enabled) {
                        onNewFocus(offset)
                        focus = offset
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun CameraControls(
    onFlashModeClick: () -> Unit,
    switchCamera: () -> Unit,
    frontCameraOn: Boolean,
    capturePhoto: () -> Unit,
    flashState: FlashState
) {
    Row(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(cameraControlsPadding),
        horizontalArrangement = Arrangement.spacedBy(cameraControlsHorizontalArrangement),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CustomRippleColor(
            color = MaterialTheme.colorScheme.tertiary
        ) {
            AnimatedContent(
                targetState = flashState,
                transitionSpec = { slideInVertically { -it } togetherWith fadeOut() }
            ) {
                val flashIcon = when (it) {
                    FlashState.ON -> Icons.Rounded.FlashOn
                    FlashState.TORCH -> Icons.Rounded.FlashlightOn
                    FlashState.OFF -> Icons.Rounded.FlashOff
                }
                MediaIconButton(
                    onClick = onFlashModeClick,
                    enabled = frontCameraOn.not(),
                    painter = rememberVectorPainter(flashIcon),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color.White
                    )
                )
            }
        }

        val haptic = LocalHapticFeedback.current

        Box(
            modifier = Modifier
                .size(cameraControlsHeight)
                .clip(CircleShape)
                .border(cameraCaptureButtonBorderWidth, Color.White, CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            capturePhoto()
                        }
                    )
            ) {
            }
        }

        CustomRippleColor(
            color = MaterialTheme.colorScheme.tertiary
        ) {
            RotatingBox(
                frontCameraOn
            ) {
                MediaIconButton(
                    onClick = switchCamera,
                    painter = rememberVectorPainter(Icons.Rounded.Cameraswitch),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                )
            }
        }
    }
}

@Composable
private fun RotatingBox(
    key: Any?,
    modifier: Modifier = Modifier,
    animateVisibilityDuringRotation: Boolean = false,
    content: @Composable () -> Unit
) {
    var rotation by remember { mutableFloatStateOf(0f) }

    var rotationInProgress by remember { mutableStateOf(false) }

    val hasInitialized = remember { mutableStateOf(false) }

    LaunchedEffect(key) {
        if (hasInitialized.value) {
            rotationInProgress = true
            animate(
                initialValue = preRotationAnimationInitialValue,
                targetValue = preRotationAnimationTargetValue,
                animationSpec = preRotationAnimationSpec
            ) { value, _ -> rotation = value }
            animate(
                initialValue = rotationAnimationInitialValue,
                targetValue = rotationAnimationTargetValue,
                animationSpec = rotationAnimationSpec

            ) { value, _ -> rotation = value }
            rotationInProgress = false
        } else {
            hasInitialized.value = true
        }
    }
    AnimatedVisibility(
        visible = rotationInProgress || animateVisibilityDuringRotation.not(),
        enter = fadeIn(tween(durationMillis = rotationVisibilityAnimationDuration)),
        exit = fadeOut(tween())
    ) {
        Box(
            modifier
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = rotationCameraDistanceMultiplier * density
                }
        ) {
            content()
        }
    }
}
