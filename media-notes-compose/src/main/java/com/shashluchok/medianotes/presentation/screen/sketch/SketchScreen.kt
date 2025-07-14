package com.shashluchok.medianotes.presentation.screen.sketch

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.shashluchok.medianotes.R
import com.shashluchok.medianotes.presentation.components.dialog.LoadingAnimationDialog
import com.shashluchok.medianotes.presentation.components.mediaicon.MediaIconButton
import com.shashluchok.medianotes.presentation.components.snackbar.SnackbarData
import com.shashluchok.medianotes.presentation.components.snackbar.SnackbarHost
import com.shashluchok.medianotes.presentation.components.topbar.MediaTopBar
import com.shashluchok.medianotes.presentation.data.ActionIcon
import com.shashluchok.medianotes.presentation.screen.sketch.SketchViewModel.Action.ChangePenSettings
import com.shashluchok.medianotes.presentation.screen.sketch.SketchViewModel.Action.OnChangeEraserSettings
import com.shashluchok.medianotes.presentation.screen.sketch.SketchViewModel.Action.OnNewBitmap
import com.shashluchok.medianotes.presentation.screen.sketch.SketchViewModel.DrawSettings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private val sketchControlsHeight = 48.dp

private const val eraserPointerCircleWidthPx = 5f

private val penSettingsPanelPadding = PaddingValues(
    start = 24.dp,
    end = 24.dp,
    bottom = 24.dp
)

private val eraserSettingsPanelPadding = PaddingValues(
    horizontal = 24.dp
)

@Composable
internal fun SketchScreen(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SketchViewModel = koinViewModel()
) {
    val state = viewModel.stateFlow.collectAsState().value

    LaunchedEffect(state.sketchSaved) {
        if (state.sketchSaved) onDismissRequest()
    }

    SketchScreen(
        modifier = modifier.fillMaxSize(),
        saveEnabled = state.saveEnabled,
        drawnPaths = state.paths,
        currentPath = state.currentPath,
        onAction = viewModel::onAction,
        drawSettings = state.currentSettings,
        undoEnabled = state.undoEnabled,
        redoEnabled = state.redoEnabled,
        isLoading = state.isLoading,
        onDismissRequest = onDismissRequest,
        snackbarData = state.snackbarData,
        drawSettingsVisible = state.showDrawSettings
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SketchScreen(
    saveEnabled: Boolean,
    drawnPaths: ImmutableList<SketchViewModel.PathData>,
    currentPath: SketchViewModel.PathData?,
    onAction: (SketchViewModel.Action) -> Unit,
    undoEnabled: Boolean,
    redoEnabled: Boolean,
    drawSettings: DrawSettings,
    isLoading: Boolean,
    snackbarData: SnackbarData?,
    drawSettingsVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit
) {
    val graphicsLayer = rememberGraphicsLayer()

    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            MediaTopBar(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                title = stringResource(R.string.screen_sketch__topbar__title),
                navigationIcon = ActionIcon(
                    painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.ArrowBack),
                    onClick = onDismissRequest
                ),
                actions = persistentListOf(
                    ActionIcon(
                        painter = rememberVectorPainter(Icons.Rounded.Save),
                        onClick = {
                            onAction(
                                SketchViewModel.Action.SaveSketch(context = context)
                            )
                        },
                        enabled = saveEnabled,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                )
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.exclude(
            WindowInsets.statusBars
        ),
        snackbarHost = {
            val snackBarHostState = remember { SnackbarHostState() }
            SnackbarHost(
                snackbarData = snackbarData,
                snackBarHostState = snackBarHostState
            )
        }

    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            SketchView(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .weight(1f),
                paths = drawnPaths,
                currentPath = currentPath,
                onSketchAction = onAction,
                graphicsLayer = graphicsLayer,
                isEraserEnabled = drawSettings is DrawSettings.Eraser
            )

            SketchControls(
                modifier = Modifier,
                onUndo = { onAction(SketchViewModel.Action.Undo) },
                onRedo = { onAction(SketchViewModel.Action.Redo) },
                undoEnabled = undoEnabled,
                redoEnabled = redoEnabled,
                onChangeEraserThickness = { onAction(OnChangeEraserSettings(it)) },
                onChangePenThickness = { onAction(ChangePenSettings.Thickness(it)) },
                onChangePenColorHue = { onAction(ChangePenSettings.Hue(it)) },
                onChangePenColorSaturationAndValue = { saturation, value ->
                    onAction(
                        ChangePenSettings.SaturationAndValue(
                            saturation,
                            value
                        )
                    )
                },
                onChangeTool = { onAction(SketchViewModel.Action.ChangeDrawingTool) },
                currentSettings = drawSettings,
                drawSettingsVisible = drawSettingsVisible,
                onChangeDrawSettingsVisibility = {
                    onAction(SketchViewModel.Action.SetDrawSettingsVisibility(it))
                }
            )
        }
    }
    if (isLoading) {
        LoadingAnimationDialog()
    }
}

@Composable
private fun SketchView(
    graphicsLayer: GraphicsLayer,
    isEraserEnabled: Boolean,
    paths: ImmutableList<SketchViewModel.PathData>,
    currentPath: SketchViewModel.PathData?,
    onSketchAction: (SketchViewModel.Action) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    Canvas(
        modifier = modifier
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                    if (currentPath == null) {
                        // Pass new bitmap only if the current path is finished.
                        scope.launch {
                            onSketchAction(OnNewBitmap(graphicsLayer.toImageBitmap()))
                        }
                    }
                }
                drawLayer(graphicsLayer)
            }
            // Otherwise eraser somehow doesn't work.
            .graphicsLayer(alpha = 0.99f)
            .clipToBounds()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        onSketchAction(SketchViewModel.Action.OnNewPathStart)
                        onSketchAction(SketchViewModel.Action.Draw(it))
                    },
                    onDragEnd = {
                        onSketchAction(SketchViewModel.Action.OnPathEnd)
                    },
                    onDrag = { change, _ ->
                        onSketchAction(SketchViewModel.Action.Draw(change.position))
                    },
                    onDragCancel = {
                        onSketchAction(
                            SketchViewModel.Action.OnPathEnd
                        )
                    }
                )
            }
    ) {
        paths.fastForEach(::drawPath)
        currentPath?.let {
            drawPath(it)
            if (isEraserEnabled) {
                drawCircle(
                    color = Color.Black,
                    style = Stroke(
                        width = eraserPointerCircleWidthPx
                    ),
                    radius = it.tool.currentThickness.toPx() / 2,
                    center = it.path.lastOrNull() ?: this.center
                )
            }
        }
    }
}

private fun DrawScope.drawPath(
    pathData: SketchViewModel.PathData
) {
    val smoothedPath = with(pathData) {
        Path().apply {
            if (path.isNotEmpty()) {
                moveTo(path.first().x, path.first().y)
                quadraticTo(
                    x1 = path.first().x,
                    y1 = path.first().y,
                    x2 = path.first().x,
                    y2 = path.first().y
                )
                for (i in 1..path.lastIndex) {
                    val from = path[i - 1]
                    val to = path[i]
                    quadraticTo(
                        x1 = from.x,
                        y1 = from.y,
                        x2 = (from.x + to.x) / 2f,
                        y2 = (from.y + to.y) / 2f
                    )
                }
            }
        }
    }

    when (val tool = pathData.tool) {
        is DrawSettings.Eraser -> {
            drawPath(
                path = smoothedPath,
                color = Color.Transparent,
                style = Stroke(
                    width = tool.currentThickness.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                blendMode = BlendMode.Clear
            )
        }

        is DrawSettings.Pen -> drawPath(
            path = smoothedPath,
            color = tool.color,
            style = Stroke(
                width = tool.currentThickness.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SketchControls(
    currentSettings: DrawSettings,
    undoEnabled: Boolean,
    redoEnabled: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onChangeEraserThickness: (Dp) -> Unit,
    onChangePenThickness: (Dp) -> Unit,
    onChangePenColorHue: (Float) -> Unit,
    onChangePenColorSaturationAndValue: (Float, Float) -> Unit,
    onChangeTool: () -> Unit,
    drawSettingsVisible: Boolean,
    onChangeDrawSettingsVisibility: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(sketchControlsHeight),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedContent(
            targetState = currentSettings,
            transitionSpec = { slideInVertically { -it } togetherWith slideOutVertically { it } }
        ) {
            val painter = when (it) {
                is DrawSettings.Eraser -> painterResource(R.drawable.ic_pen)
                is DrawSettings.Pen -> painterResource(R.drawable.ic_eraser)
            }

            MediaIconButton(
                painter = painter,
                onClick = onChangeTool
            )
        }

        Row {
            MediaIconButton(
                enabled = undoEnabled,
                painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.Undo),
                onClick = onUndo
            )
            MediaIconButton(
                enabled = redoEnabled,
                painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.Redo),
                onClick = onRedo
            )
        }

        MediaIconButton(
            painter = rememberVectorPainter(Icons.Rounded.Settings),
            onClick = {
                onChangeDrawSettingsVisibility(true)
            }
        )
        if (drawSettingsVisible) {
            ModalBottomSheet(
                onDismissRequest = { onChangeDrawSettingsVisibility(false) },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                when (currentSettings) {
                    is DrawSettings.Eraser -> EraserSettingsPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(penSettingsPanelPadding),
                        eraserSettings = currentSettings,
                        onChangeEraserThickness = onChangeEraserThickness
                    )

                    is DrawSettings.Pen -> PenSettingsPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(eraserSettingsPanelPadding),
                        penSettings = currentSettings,
                        onChangePenThickness = onChangePenThickness,
                        onChangePenColorHue = onChangePenColorHue,
                        onChangePenColorSaturationAndValue = onChangePenColorSaturationAndValue
                    )
                }
            }
        }
    }
}
