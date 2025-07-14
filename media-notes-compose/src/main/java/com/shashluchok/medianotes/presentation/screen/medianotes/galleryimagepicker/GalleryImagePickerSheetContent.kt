@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.shashluchok.medianotes.presentation.screen.medianotes.galleryimagepicker

import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.ScaleToBounds
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.shashluchok.medianotes.R
import com.shashluchok.medianotes.presentation.MediaImage
import com.shashluchok.medianotes.presentation.SharedTransitionUtils.SharedElementType
import com.shashluchok.medianotes.presentation.SharedTransitionUtils.getKeyByElementType
import com.shashluchok.medianotes.presentation.components.AnimatedVisibilityOnDisplay
import com.shashluchok.medianotes.presentation.components.LocalIsCompact
import com.shashluchok.medianotes.presentation.components.LocalNavAnimatedVisibilityScope
import com.shashluchok.medianotes.presentation.components.LocalSharedTransitionScope
import com.shashluchok.medianotes.presentation.components.camera.CameraPreview
import com.shashluchok.medianotes.presentation.components.camera.rememberPreviewView
import com.shashluchok.medianotes.presentation.components.topbar.MediaTopBar
import com.shashluchok.medianotes.presentation.data.ActionIcon
import com.shashluchok.medianotes.presentation.modifiers.scrollbars.ScrollbarConfig
import com.shashluchok.medianotes.presentation.modifiers.scrollbars.verticalScrollbar
import com.shashluchok.medianotes.presentation.modifiers.shadow.ShadowPosition
import com.shashluchok.medianotes.presentation.modifiers.shadow.shadow
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private const val compactSpanCount = 3
private const val wideSpanCount = 4

private val scrollBarPadding = PaddingValues(end = 4.dp)

private val imagePlaceholderIconSize = 40.dp
private const val imagePlaceholderIconAlpha = 0.3f

private val gridPadding = PaddingValues(
    horizontal = 12.dp,
    vertical = 6.dp
)
private val gridItemsArrangement = Arrangement.spacedBy(6.dp)
private val gridItemShape = RoundedCornerShape(24.dp)
private val gridItemBorderThickness = 1.dp
private const val gridItemBorderColorAlpha = 0.12f

private const val cameraItemKey = "camera"
private val cameraPreviewMaskColor = Color.Black.copy(alpha = 0.3f)
private val cameraPreviewMaskIconColor = Color.White
private val cameraPreviewMaskIconSize = 40.dp

private val cameraEnterAnimationInSpec = fadeIn(
    tween(
        delayMillis = 600,
        durationMillis = 600
    )
)

@Composable
internal fun ImagePickerSheetContent(
    topBarVisible: Boolean,
    onOpenCamera: () -> Unit,
    onOpenImage: (MediaImage) -> Unit,
    onDismiss: () -> Unit,
    scrollEnabled: Boolean,
    onCanScrollBackward: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GalleryImagePickerViewModel = koinViewModel()
) {
    val state = viewModel.stateFlow.collectAsState().value

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onAction(GalleryImagePickerViewModel.Action.OnDisplay(context))
    }

    ImagePickerSheetContent(
        modifier = modifier,
        images = state.images,
        onOpenCamera = onOpenCamera,
        onOpenImage = onOpenImage,
        scrollEnabled = scrollEnabled,
        onCanScrollBackward = onCanScrollBackward,
        onDismiss = onDismiss,
        topBarVisible = topBarVisible
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImagePickerSheetContent(
    onOpenCamera: () -> Unit,
    onOpenImage: (MediaImage) -> Unit,
    onDismiss: () -> Unit,
    scrollEnabled: Boolean,
    onCanScrollBackward: (Boolean) -> Unit,
    topBarVisible: Boolean,
    images: ImmutableList<MediaImage>,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()

    LaunchedEffect(Unit) {
        snapshotFlow { gridState.canScrollBackward }.collect {
            onCanScrollBackward(it)
        }
    }

    val sheetContainerColor by animateColorAsState(
        targetValue = if (topBarVisible) {
            MaterialTheme.colorScheme.surface
        } else {
            BottomSheetDefaults.ContainerColor
        }
    )
    Column(
        modifier = modifier.background(sheetContainerColor)
    ) {
        AnimatedContent(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f),
            targetState = topBarVisible,
            transitionSpec = {
                slideInVertically {
                    if (topBarVisible) -it else it
                } + fadeIn() togetherWith fadeOut()
            }
        ) { isTopBarVisible ->
            if (isTopBarVisible) {
                MediaTopBar(
                    modifier = Modifier
                        .shadow(
                            shadowPositions = persistentSetOf(ShadowPosition.BOTTOM),
                            shadowVisible = gridState.canScrollBackward
                        )
                        .background(MaterialTheme.colorScheme.surface),
                    title = stringResource(R.string.screen_gallery_images__topbar__title),
                    navigationIcon = ActionIcon(
                        painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.ArrowBack),
                        onClick = onDismiss
                    )
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    BottomSheetDefaults.DragHandle()
                }
            }
        }
        ImagesLazyList(
            modifier = Modifier.weight(1f),
            onCameraClick = onOpenCamera,
            onImageClick = onOpenImage,
            gridState = gridState,
            scrollEnabled = scrollEnabled,
            images = images
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ImagesLazyList(
    images: ImmutableList<MediaImage>,
    gridState: LazyGridState,
    onCameraClick: () -> Unit,
    onImageClick: (MediaImage) -> Unit,
    scrollEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val spanCount = if (LocalIsCompact.current) {
        compactSpanCount
    } else {
        wideSpanCount
    }

    val previewView = rememberPreviewView()

    val scope = rememberCoroutineScope()

    with(LocalSharedTransitionScope.current) {
        LazyVerticalGrid(
            modifier = modifier
                .verticalScrollbar(
                    state = gridState,
                    spans = spanCount,
                    config = ScrollbarConfig(
                        padding = scrollBarPadding
                    )
                ),
            columns = GridCells.Fixed(spanCount),
            state = gridState,
            verticalArrangement = gridItemsArrangement,
            horizontalArrangement = gridItemsArrangement,
            contentPadding = gridPadding,
            userScrollEnabled = scrollEnabled && isTransitionActive.not()
        ) {
            item(key = cameraItemKey) {
                CameraItem(
                    modifier = Modifier.sharedElement(
                        state = rememberSharedContentState(
                            key = getKeyByElementType(SharedElementType.Camera)
                        ),
                        animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
                    ),
                    onClick = {
                        scope.launch {
                            // To make item fully visible before shared element transition
                            gridState.scrollToItemIfNotVisible(cameraItemKey)
                            onCameraClick()
                        }
                    },
                    previewView = previewView
                )
            }
            items(
                items = images,
                key = { image -> image.id }
            ) { image ->

                GridItem {
                    Icon(
                        painter = rememberVectorPainter(Icons.Outlined.Image),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = imagePlaceholderIconAlpha),
                        modifier = Modifier.size(imagePlaceholderIconSize)
                    )
                    Image(
                        modifier = Modifier
                            .sharedBounds(
                                rememberSharedContentState(
                                    key = getKeyByElementType(
                                        SharedElementType.Image(image)
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
                            )
                            .fillMaxSize()
                            .clickable {
                                // To make item fully visible before shared element transition
                                scope.launch {
                                    gridState.scrollToItemIfNotVisible(image.id)
                                    onImageClick(image)
                                }
                            }
                            .clip(gridItemShape),
                        painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(image.path)
                                .crossfade(true)
                                .memoryCacheKey(image.id)
                                .build()
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraItem(
    onClick: () -> Unit,
    previewView: PreviewView,
    modifier: Modifier = Modifier
) {
    GridItem(
        modifier = modifier
    ) {
        AnimatedVisibilityOnDisplay(
            enter = cameraEnterAnimationInSpec
        ) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                previewView = previewView
            )
        }
        CameraPreviewMask(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        )
    }
}

@Composable
private fun CameraPreviewMask(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(cameraPreviewMaskColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(cameraPreviewMaskIconSize),
            painter = rememberVectorPainter(Icons.Filled.PhotoCamera),
            tint = cameraPreviewMaskIconColor,
            contentDescription = null
        )
    }
}

@Composable
private fun GridItem(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(gridItemShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(
                width = gridItemBorderThickness,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = gridItemBorderColorAlpha),
                shape = gridItemShape
            )
            .aspectRatio(1f),
        content = content,
        contentAlignment = Alignment.Center
    )
}

private suspend fun LazyGridState.scrollToItemIfNotVisible(itemKey: String) {
    val offset = layoutInfo.visibleItemsInfo.find {
        it.key == itemKey
    }?.offset?.y ?: 0
    if (offset < 0) animateScrollBy(offset.toFloat())
}
