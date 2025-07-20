@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.shashluchok.medianotes.presentation.screen.imageeditor

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.ScaleToBounds
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.CropRotate
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.maxBitmapSize
import coil3.size.Size
import coil3.toBitmap
import com.shashluchok.medianotes.R
import com.shashluchok.medianotes.presentation.MediaImage
import com.shashluchok.medianotes.presentation.SharedTransitionUtils
import com.shashluchok.medianotes.presentation.components.AnimatedVisibilityOnDisplay
import com.shashluchok.medianotes.presentation.components.LocalIsCompact
import com.shashluchok.medianotes.presentation.components.LocalNavAnimatedVisibilityScope
import com.shashluchok.medianotes.presentation.components.LocalSharedTransitionScope
import com.shashluchok.medianotes.presentation.components.counter.Counter
import com.shashluchok.medianotes.presentation.components.cropper.cropper.ImageCropper
import com.shashluchok.medianotes.presentation.components.cropper.cropper.cropdefaults.CropDefaults
import com.shashluchok.medianotes.presentation.components.cropper.cropper.model.CropAspectRatio
import com.shashluchok.medianotes.presentation.components.cropper.cropper.model.CropData
import com.shashluchok.medianotes.presentation.components.cropper.cropper.model.RectCropShape
import com.shashluchok.medianotes.presentation.components.cropper.cropper.state.rememberCropState
import com.shashluchok.medianotes.presentation.components.dialog.LoadingAnimationDialog
import com.shashluchok.medianotes.presentation.components.dialog.MediaAlertDialog
import com.shashluchok.medianotes.presentation.components.mediaicon.MediaIconButtonDefaults
import com.shashluchok.medianotes.presentation.components.rememberSystemBarsController
import com.shashluchok.medianotes.presentation.components.snackbar.SnackbarHost
import com.shashluchok.medianotes.presentation.data.ActionIcon
import com.shashluchok.medianotes.presentation.modifiers.scrollbars.ScrollbarConfig
import com.shashluchok.medianotes.presentation.modifiers.scrollbars.verticalScrollbar
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koin.androidx.compose.koinViewModel

private const val topBarDisplayDelayMillis = 100
private const val topBarExitAnimationDuration = 0

private val imageCropRectMinSize = IntSize(width = 800, height = 600)
private const val imageCropHandleSizePx = 100f
private const val imageCropMaxZoom = 10f

private val imageEditorMaxBitmapSize = Size(1080, 1920)

private val imageTextBoxMaxHeight = 200.dp
private val imageTextBoxOuterPadding = PaddingValues(16.dp)
private val imageTextBoxShape = RoundedCornerShape(12.dp)
private const val imageTextBoxBackgroundAlpha = 0.12f
private val imageTextBoxInnerPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
private val imageTextBoxScrollbarConfig = ScrollbarConfig(
    thickness = 4.dp,
    padding = PaddingValues(end = 4.dp)
)

@Composable
internal fun ImageEditorScreen(
    image: MediaImage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ImageEditorViewModel = koinViewModel()
) {
    val state = viewModel.stateFlow.collectAsState().value

    val context = LocalContext.current
    val systemBarsController = rememberSystemBarsController()
    val darkTheme = isSystemInDarkTheme()

    DisposableEffect(systemBarsController, darkTheme) {
        systemBarsController.isStatusBarAppearanceLight = false
        systemBarsController.isNavigationBarsAppearanceLight = false
        onDispose {
            systemBarsController.isStatusBarAppearanceLight = darkTheme.not()
            systemBarsController.isNavigationBarsAppearanceLight = darkTheme.not()
        }
    }

    LaunchedEffect(image) {
        viewModel.onAction(ImageEditorViewModel.Action.OnDisplay(context, image))
    }

    LaunchedEffect(state.isImageSaved) {
        if (state.isImageSaved) onDismiss()
    }

    LaunchedEffect(state.shouldDismiss) {
        if (state.shouldDismiss) onDismiss()
    }

    ImageEditorScreen(
        modifier = modifier,
        onDismiss = onDismiss,
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
private fun ImageEditorScreen(
    onDismiss: () -> Unit,
    onAction: (ImageEditorViewModel.Action) -> Unit,
    state: ImageEditorViewModel.State,
    modifier: Modifier = Modifier
) {
    BackHandler(
        enabled = state.isCropping,
        onBack = {
            onAction(ImageEditorViewModel.Action.OnCroppingChange(false))
        }
    )

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            val snackBarHostState = remember { SnackbarHostState() }
            SnackbarHost(
                snackbarData = state.snackbarData,
                snackBarHostState = snackBarHostState
            )
        },
        content = {
            ImageEditor(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .background(Color.Black),
                images = state.images,
                currentPageIndex = state.currentImageIndex,
                onDismiss = onDismiss,
                onImageSelected = {
                    onAction(ImageEditorViewModel.Action.OnImageSelected(it))
                },
                onSendCLick = { context, layoutDirection, density ->
                    onAction(
                        ImageEditorViewModel.Action.OnSendClick(
                            context,
                            layoutDirection,
                            density
                        )
                    )
                },
                onCaptionChange = {
                    onAction(ImageEditorViewModel.Action.OnCaptionChange(it))
                },
                caption = state.caption,
                isCropping = state.isCropping,
                onRotate = {
                    onAction(ImageEditorViewModel.Action.OnRotateClick)
                },
                onCropChange = {
                    onAction(ImageEditorViewModel.Action.OnCroppingChange(it))
                },
                rotation = state.rotation,
                onRotating = {
                    onAction(ImageEditorViewModel.Action.OnRotating(it))
                },
                rotating = state.rotating,
                onCropDataChange = {
                    onAction(ImageEditorViewModel.Action.OnCropDataChange(it))
                },
                isLoading = state.isLoading,
                cropEnabled = state.cropEnabled,
                deleteEnabled = state.deleteEnabled,
                onDeleteMediaNoteClick = {
                    onAction(ImageEditorViewModel.Action.OnDeleteMediaNoteClick)
                }
            )
        }
    )
    state.alertDialogData?.let {
        MediaAlertDialog(it)
    }
}

@Composable
private fun ImageEditor(
    images: ImmutableList<MediaImage>,
    currentPageIndex: Int,
    onCropChange: (Boolean) -> Unit,
    isCropping: Boolean,
    onSendCLick: (Context, LayoutDirection, Density) -> Unit,
    onRotate: () -> Unit,
    onImageSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    onCaptionChange: (String) -> Unit,
    caption: String,
    rotation: Float,
    onRotating: (Boolean) -> Unit,
    rotating: Boolean,
    onCropDataChange: (CropData) -> Unit,
    cropEnabled: Boolean,
    deleteEnabled: Boolean,
    onDeleteMediaNoteClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        ImagePager(
            modifier = Modifier.fillMaxSize(),
            images = images,
            currentPageIndex = currentPageIndex,
            onImageSelected = onImageSelected,
            isCropping = isCropping,
            rotation = rotation,
            onRotating = onRotating,
            onCropDataChange = onCropDataChange
        )

        val context = LocalContext.current
        val density = LocalDensity.current
        val layoutDirection = LocalLayoutDirection.current

        if (LocalSharedTransitionScope.current.isTransitionActive.not()) {
            AnimatedVisibilityOnDisplay(
                enter = slideInVertically(tween(delayMillis = topBarDisplayDelayMillis)) { -it },
                exit = fadeOut(tween(topBarExitAnimationDuration))
            ) {
                ImageViewerTopBar(
                    title = {
                        AnimatedContent(
                            targetState = isCropping,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    slideInVertically { -it } togetherWith slideOutVertically { it }
                                } else {
                                    slideInVertically { it } togetherWith slideOutVertically { -it }
                                }
                            }
                        ) {
                            if (it) {
                                Text(
                                    text = stringResource(R.string.screen_image_editor__topbar__crop__title),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                            } else {
                                Counter(
                                    current = currentPageIndex + 1,
                                    total = images.size,
                                    contentColor = Color.White
                                )
                            }
                        }
                    },
                    navIcon = if (isCropping) {
                        ActionIcon(
                            onClick = { onCropChange(false) },
                            painter = rememberVectorPainter(Icons.Rounded.Close),
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color.White
                            )
                        )
                    } else {
                        ActionIcon(
                            onClick = onDismiss,
                            painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.ArrowBack),
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color.White
                            )
                        )
                    },
                    actions = when {
                        isCropping -> persistentListOf(
                            ActionIcon(
                                painter = rememberVectorPainter(Icons.Rounded.CropRotate),
                                onClick = onRotate,
                                colors = MediaIconButtonDefaults.iconButtonColors(
                                    contentColor = Color.White
                                ),
                                enabled = rotating.not()
                            ),
                            ActionIcon(
                                painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.Send),
                                onClick = {
                                    onSendCLick(context, layoutDirection, density)
                                },
                                colors = MediaIconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        )

                        cropEnabled -> {
                            persistentListOf(
                                ActionIcon(
                                    painter = rememberVectorPainter(Icons.Rounded.Crop),
                                    onClick = {
                                        onCropChange(true)
                                    },
                                    colors = MediaIconButtonDefaults.iconButtonColors(
                                        contentColor = Color.White
                                    )
                                )
                            )
                        }

                        deleteEnabled -> {
                            persistentListOf(
                                ActionIcon(
                                    painter = rememberVectorPainter(Icons.Rounded.Delete),
                                    onClick = onDeleteMediaNoteClick,
                                    colors = MediaIconButtonDefaults.iconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                )
                            )
                        }

                        else -> persistentListOf()
                    }
                )
            }
        }

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            visible = LocalSharedTransitionScope.current.isTransitionActive.not() && isCropping,
            enter = slideInVertically(tween()) { it },
            exit = slideOutVertically(tween()) { it }
        ) {
            Caption(
                onCaptionChange = onCaptionChange,
                caption = caption
            )
        }
        if (isLoading) {
            LoadingAnimationDialog()
        }
    }
}

@Composable
private fun ImagePager(
    images: ImmutableList<MediaImage>,
    onImageSelected: (Int) -> Unit,
    currentPageIndex: Int,
    isCropping: Boolean,
    onRotating: (Boolean) -> Unit,
    onCropDataChange: (CropData) -> Unit,
    modifier: Modifier = Modifier,
    rotation: Float = 0f
) {
    val pagerState = rememberPagerState(
        pageCount = {
            images.size
        },
        initialPage = currentPageIndex
    )

    LaunchedEffect(images) {
        pagerState.scrollToPage(currentPageIndex)
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (pagerState.pageCount > 0) {
                onImageSelected(page)
            }
        }
    }

    with(LocalSharedTransitionScope.current) {
        HorizontalPager(
            modifier = modifier,
            state = pagerState,
            verticalAlignment = Alignment.Top,
            userScrollEnabled = isTransitionActive.not() && isCropping.not(),
            key = { images[it].id }
        ) { pageIndex ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(images.lastIndex - pageIndex.toFloat())
                    .graphicsLayer {
                        val pageOffset =
                            pagerState.currentPage - pageIndex + pagerState.currentPageOffsetFraction
                        scaleX = if (pageOffset > 0) 1f else 1 + pageOffset / 4
                        scaleY = if (pageOffset > 0) 1f else 1 + pageOffset / 4
                        alpha = if (pageOffset > 0) 1f else 1 + pageOffset
                        translationX = if (pageOffset > 0) 0f else size.width * pageOffset
                    },
                contentAlignment = Alignment.Center
            ) {
                val imageBitmap = rememberImageBitmap(
                    images[pageIndex].path
                )

                imageBitmap?.let {
                    val cropState = rememberCropState(
                        cropProperties = CropDefaults.properties(
                            maxZoom = imageCropMaxZoom,
                            minSize = imageCropRectMinSize,
                            cropShape = RectCropShape(),
                            handleSize = imageCropHandleSizePx,
                            aspectRatio = CropAspectRatio.RATIO_4_3
                        ),
                        onCropDataChange = onCropDataChange,
                        imageBitmap = imageBitmap,
                        contentScale = if (LocalIsCompact.current) {
                            ContentScale.FillWidth
                        } else {
                            ContentScale.FillHeight
                        }
                    )

                    ImageCropper(
                        modifier = Modifier.sharedBounds(
                            rememberSharedContentState(
                                key = SharedTransitionUtils.getKeyByElementType(
                                    SharedTransitionUtils.SharedElementType.Image(
                                        images[pageIndex]
                                    )
                                )
                            ),
                            resizeMode = ScaleToBounds(
                                if (LocalIsCompact.current) {
                                    ContentScale.FillWidth
                                } else {
                                    ContentScale.FillHeight
                                }
                            ),
                            animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
                        ),
                        contentDescription = null,
                        cropStyle = CropDefaults.style(),
                        cropState = cropState,
                        isCropping = isCropping,
                        rotation = rotation,
                        onRotating = onRotating
                    )

                    val imageText = images[pageIndex].text

                    AnimatedVisibility(
                        visible = imageText.isNotEmpty() && pagerState.currentPageOffsetFraction == 0f,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        enter = fadeIn(tween()) + slideInVertically(tween()) { it },
                        exit = fadeOut(tween())
                    ) {
                        ImageTextBox(text = imageText)
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageTextBox(
    text: String,
    modifier: Modifier = Modifier
) {
    val scrollable = rememberScrollState()
    Box(
        modifier = modifier
            .navigationBarsPadding()
            .heightIn(max = imageTextBoxMaxHeight)
            .padding(imageTextBoxOuterPadding)
            .clip(imageTextBoxShape)
            .background(
                color = Color.White.copy(alpha = imageTextBoxBackgroundAlpha)
            )
            .verticalScrollbar(
                state = scrollable,
                config = imageTextBoxScrollbarConfig,
                alwaysVisible = true
            )
            .verticalScroll(scrollable)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(imageTextBoxInnerPadding),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White
            ),
            text = text
        )
    }
}

@Composable
private fun rememberImageBitmap(path: String): ImageBitmap? {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(path) {
        val imageLoader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(path)
            .maxBitmapSize(imageEditorMaxBitmapSize)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()

        val result = imageLoader.execute(request)
        bitmap = (result as? SuccessResult)?.image?.toBitmap()?.asImageBitmap()
    }

    return bitmap
}
