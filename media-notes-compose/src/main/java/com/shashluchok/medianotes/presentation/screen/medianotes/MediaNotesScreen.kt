@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)

package com.shashluchok.medianotes.presentation.screen.medianotes

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.shashluchok.medianotes.presentation.CAMERA_AND_IMAGES
import com.shashluchok.medianotes.presentation.MediaImage
import com.shashluchok.medianotes.presentation.components.mediaicon.MediaIconButton
import com.shashluchok.medianotes.presentation.components.mediaicon.MediaIconButtonDefaults
import com.shashluchok.medianotes.presentation.components.scrim.Scrim
import com.shashluchok.medianotes.presentation.components.snackbar.SnackbarData
import com.shashluchok.medianotes.presentation.components.snackbar.SnackbarHost
import com.shashluchok.medianotes.presentation.components.topbar.MediaTopAppBar
import com.shashluchok.medianotes.presentation.modifiers.shadow.ShadowPosition
import com.shashluchok.medianotes.presentation.modifiers.shadow.shadow
import com.shashluchok.medianotes.presentation.screen.medianotes.MediaNotesViewModel.Action
import com.shashluchok.medianotes.presentation.screen.medianotes.MediaNotesViewModel.Selection
import com.shashluchok.medianotes.presentation.screen.medianotes.MediaNotesViewModel.SelectionOption
import com.shashluchok.medianotes.presentation.screen.medianotes.galleryimagepicker.ImagePickerSheetContent
import com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist.MediaNotesList
import com.shashluchok.medianotes.presentation.screen.medianotes.mediatoolbar.MediaToolbar
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private const val topBarIconChangeAnimationDuration = 50

private val dividerThickness = 1.dp

private val toolbarMinHeight = 48.dp

private val bottomSheetCornerRadius = 28.dp

@Composable
internal fun MediaListScreen(
    onOpenCamera: () -> Unit,
    onOpenImage: (MediaImage) -> Unit,
    onSketchClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MediaNotesViewModel = koinViewModel()
) {
    val state = viewModel.stateFlow.collectAsState().value

    MediaListScreen(
        modifier = modifier,
        onOpenImage = onOpenImage,
        onOpenCamera = onOpenCamera,
        onSketchClick = onSketchClick,
        snackbarData = state.snackbarData,
        onCameraPermissionDenied = {
            viewModel.onAction(Action.OnCameraPermissionDenied)
        },
        onPermissionRequestUnavailable = { context ->
            viewModel.onAction(Action.OnRequestPermissionUnavailable(context))
        },
        notes = state.notes,
        selection = state.selection,
        topBarTitle = state.topBarTitle,
        onRecordAudioPermissionDenied = {
            viewModel.onAction(Action.OnRecordAudioPermissionDenied)
        },
        editableMediaNoteItem = state.editingMediaNote as? MediaNoteItem.Text,
        onAction = viewModel::onAction
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun MediaListScreen(
    snackbarData: SnackbarData?,
    onCameraPermissionDenied: () -> Unit,
    onRecordAudioPermissionDenied: () -> Unit,
    onPermissionRequestUnavailable: (Context) -> Unit,
    onSketchClick: () -> Unit,
    onOpenCamera: () -> Unit,
    onOpenImage: (MediaImage) -> Unit,
    notes: ImmutableList<MediaNoteItem>,
    selection: Selection?,
    topBarTitle: String,
    editableMediaNoteItem: MediaNoteItem.Text?,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomSheetState = rememberStandardBottomSheetState(
        skipHiddenState = false,
        initialValue = SheetValue.Hidden
    )

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = bottomSheetState
    )

    val scope = rememberCoroutineScope()

    BackHandler(
        enabled = selection != null || bottomSheetState.isVisible
    ) {
        when {
            bottomSheetState.isVisible -> scope.launch {
                bottomSheetState.hide()
            }

            selection != null -> onAction(Action.OnCancelSelecting)
        }
    }

    var sheetSwipeEnabled by remember {
        mutableStateOf(true)
    }

    val topBarVisible by remember {
        derivedStateOf {
            bottomSheetState.currentValue == SheetValue.Expanded &&
                bottomSheetState.targetValue != SheetValue.PartiallyExpanded
        }
    }

    val sheetCorners by animateDpAsState(
        targetValue = if (topBarVisible) 0.dp else bottomSheetCornerRadius,
        animationSpec = tween()
    )

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val imagesPermission = rememberMultiplePermissionsState(
        CAMERA_AND_IMAGES
    )

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        if (permissionsMap.any { it.value.not() }) {
            onCameraPermissionDenied()
        } else {
            scope.launch {
                scaffoldState.bottomSheetState.partialExpand()
            }
            keyboardController?.hide()
        }
    }

    BoxWithConstraints {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            modifier = modifier,
            containerColor = Color.Unspecified,
            sheetPeekHeight = (maxHeight / 2),
            sheetSwipeEnabled = sheetSwipeEnabled,
            sheetDragHandle = null,
            sheetShape = RoundedCornerShape(sheetCorners),
            sheetContent = {
                if (scaffoldState.bottomSheetState.isVisible) {
                    SheetContent(
                        onOpenCamera = onOpenCamera,
                        onOpenImage = onOpenImage,
                        onDismiss = {
                            scope.launch { bottomSheetState.hide() }
                        },
                        scrollEnabled = bottomSheetState.currentValue == SheetValue.Expanded,
                        onCanScrollBackward = { canScroll ->
                            sheetSwipeEnabled = canScroll.not()
                        },
                        topBarVisible = topBarVisible
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(
                    snackbarData = snackbarData,
                    snackBarHostState = scaffoldState.snackbarHostState
                )
            }
        ) {
            Content(
                modifier = Modifier.fillMaxSize(),
                onCameraClick = {
                    if (imagesPermission.allPermissionsGranted) {
                        scope.launch {
                            scaffoldState.bottomSheetState.partialExpand()
                        }
                        keyboardController?.hide()
                    } else {
                        if (imagesPermission.shouldShowRationale) {
                            onPermissionRequestUnavailable(context)
                        } else {
                            requestPermissionLauncher.launch(
                                CAMERA_AND_IMAGES.toTypedArray()
                            )
                        }
                    }
                },
                onSketchClick = onSketchClick,
                onNavigationIconClick = {
                    onAction(Action.OnNavigationIconClick)
                },
                topBarTitle = topBarTitle,
                onSelect = {
                    onAction(Action.OnSelectMediaNote(it))
                },
                notes = notes,
                selection = selection,
                onSelectionOptionClick = {
                    onAction(Action.OnSelectionOptionClick(it))
                },
                onOpenImage = onOpenImage,
                onRecordAudioPermissionDenied = onRecordAudioPermissionDenied,
                onPermissionRequestUnavailable = {
                    onPermissionRequestUnavailable(context)
                }
//                editableMediaNoteItem = editableMediaNoteItem
            )
            val scrimVisible = scaffoldState.bottomSheetState.run {
                isVisible || targetValue == SheetValue.PartiallyExpanded
            }
            Scrim(
                isVisible = scrimVisible,
                modifier = Modifier.fillMaxSize(),
                onClick = {
                    scope.launch {
                        bottomSheetState.hide()
                    }
                },
                color = BottomSheetDefaults.ScrimColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    onSketchClick: () -> Unit,
    onCameraClick: () -> Unit,
    onNavigationIconClick: () -> Unit,
    topBarTitle: String,
    selection: Selection?,
    onRecordAudioPermissionDenied: () -> Unit,
    onPermissionRequestUnavailable: () -> Unit,
    notes: ImmutableList<MediaNoteItem>,
    onSelect: (noteItem: MediaNoteItem) -> Unit,
    onOpenImage: (MediaImage) -> Unit,
    onSelectionOptionClick: (SelectionOption) -> Unit,
    modifier: Modifier = Modifier
//    editableMediaNoteItem: MediaNoteItem.Text?
) {
    val lazyListState = rememberLazyListState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        AnimatedContent(
            modifier = Modifier
                .zIndex(2f)
                .shadow(
                    shadowPositions = persistentSetOf(ShadowPosition.BOTTOM),
                    shadowVisible = lazyListState.canScrollBackward
                )
                .background(MaterialTheme.colorScheme.surface),
            targetState = selection != null,
            transitionSpec = {
                if (targetState) {
                    slideInHorizontally(tween()) { -it } + fadeIn() togetherWith fadeOut(
                        tween(durationMillis = topBarIconChangeAnimationDuration)
                    )
                } else {
                    fadeIn(
                        tween()
                    ) togetherWith fadeOut(
                        tween(durationMillis = topBarIconChangeAnimationDuration)
                    )
                }
            }
        ) { isSelecting ->

            val navigationIcon = if (isSelecting) {
                rememberVectorPainter(Icons.Rounded.Close)
            } else {
                rememberVectorPainter(Icons.AutoMirrored.Rounded.ArrowBack)
            }
            MediaTopAppBar(
                title = topBarTitle,
                navigationIconPainter = navigationIcon,
                onNavigationIconClick = onNavigationIconClick
            )
        }

        MediaNotesList(
            modifier = Modifier.weight(1f),
            listState = lazyListState,
            onSelect = onSelect,
            notes = notes,
            selectedNotes = selection?.notes ?: persistentListOf(),
            onOpenImage = onOpenImage
        )

        Column(
            modifier = Modifier
                .shadow(
                    shadowPositions = persistentSetOf(ShadowPosition.TOP),
                    shadowVisible = lazyListState.canScrollForward
                )
                .background(MaterialTheme.colorScheme.surface)
                .windowInsetsPadding(WindowInsets.safeDrawing.exclude(WindowInsets.statusBars))

        ) {
            HorizontalDivider(thickness = dividerThickness)

            AnimatedContent(
                modifier = Modifier.heightIn(min = toolbarMinHeight),
                targetState = selection != null,
                transitionSpec = {
                    if (targetState) {
                        slideInHorizontally(tween()) { it } + fadeIn() togetherWith
                            slideOutVertically(tween()) { it } + fadeOut()
                    } else {
                        slideInVertically(tween()) { it } + fadeIn() togetherWith
                            slideOutHorizontally(tween()) { it } + fadeOut()
                    }
                }
            ) { state ->
                if (state) {
                    SelectionToolBar(
                        modifier = Modifier.fillMaxWidth(),
                        options = selection?.options ?: persistentSetOf(),
                        onOptionClick = onSelectionOptionClick
                    )
                } else {
                    MediaToolbar(
                        modifier = Modifier.fillMaxWidth(),
                        onCameraClick = onCameraClick,
                        onSketchClick = onSketchClick,
                        onRecordAudioPermissionDenied = onRecordAudioPermissionDenied,
                        onRecordAudioPermissionUnavailable = onPermissionRequestUnavailable,
                        editableTextNote = null
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionToolBar(
    options: ImmutableSet<SelectionOption>,
    onOptionClick: (SelectionOption) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        reverseLayout = true
    ) {
        itemsIndexed(
            items = options.toList(),
            key = { index, _ -> index }
        ) { _, option ->

            MediaIconButton(
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(),
                    fadeOutSpec = spring()
                ),
                painter = option.toIconPainter(),
                onClick = {
                    onOptionClick(option)
                },
                colors = MediaIconButtonDefaults.iconButtonColors(
                    contentColor = if (option == SelectionOption.DELETE) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            )
        }
    }
}

@Composable
private fun SheetContent(
    onOpenCamera: () -> Unit,
    onOpenImage: (MediaImage) -> Unit,
    onDismiss: () -> Unit,
    scrollEnabled: Boolean,
    onCanScrollBackward: (Boolean) -> Unit,
    topBarVisible: Boolean,
    modifier: Modifier = Modifier
) {
    ImagePickerSheetContent(
        modifier = modifier,
        onOpenCamera = onOpenCamera,
        onOpenImage = onOpenImage,
        onDismiss = onDismiss,
        scrollEnabled = scrollEnabled,
        onCanScrollBackward = onCanScrollBackward,
        topBarVisible = topBarVisible
    )
}

@Composable
private fun SelectionOption.toIconPainter(): Painter {
    val imageVector = when (this) {
//        SelectionOption.COPY -> Icons.Outlined.ContentCopy
//        SelectionOption.EDIT -> Icons.Outlined.ModeEdit
        SelectionOption.DELETE -> Icons.Outlined.Delete
    }
    return rememberVectorPainter(imageVector)
}
