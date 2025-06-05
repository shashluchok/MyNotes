package com.shashluchok.medianotes.presentation.screen.sketch

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.lifecycle.viewModelScope
import com.shashluchok.medianotes.R
import com.shashluchok.medianotes.data.MediaNote
import com.shashluchok.medianotes.domain.file.SaveBitmapToFileInteractor
import com.shashluchok.medianotes.domain.notes.create.CreateMediaNoteInteractor
import com.shashluchok.medianotes.presentation.components.snackbar.SnackbarData
import com.shashluchok.medianotes.presentation.screen.AbsViewModel
import com.shashluchok.medianotes.presentation.utils.middle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue

internal class SketchViewModel(
    private val createMediaNote: CreateMediaNoteInteractor,
    private val saveBitmapToFile: SaveBitmapToFileInteractor
) : AbsViewModel<SketchViewModel.State>() {

    data class State(
        val saveEnabled: Boolean = false,
        val drawing: Boolean = true,
        val currentPath: PathData? = null,
        val paths: ImmutableList<PathData> = persistentListOf(),
        val undoPaths: ImmutableList<PathData> = persistentListOf(),
        val isLoading: Boolean = false,
        val sketchSaved: Boolean = false,
        val snackbarData: SnackbarData? = null,
        val showDrawSettings: Boolean = false,
        internal val pen: DrawSettings.Pen = DrawSettings.Pen(),
        internal val eraser: DrawSettings.Eraser = DrawSettings.Eraser()
    ) {
        val redoEnabled = undoPaths.isNotEmpty()
        val undoEnabled = paths.isNotEmpty()
        val currentSettings: DrawSettings = if (drawing) pen else eraser
    }

    data class PathData(
        val id: String,
        val tool: DrawSettings,
        val path: ImmutableList<Offset>
    )

    sealed class DrawSettings {
        abstract val currentThickness: Dp
        abstract val thicknessRange: ClosedRange<Dp>

        data class Eraser(
            override val thicknessRange: ClosedRange<Dp> = ERASER_THICKNESS_RANGE,
            override val currentThickness: Dp = thicknessRange.middle
        ) : DrawSettings()

        data class Pen(
            override val thicknessRange: ClosedRange<Dp> = PEN_THICKNESS_RANGE,
            override val currentThickness: Dp = thicknessRange.middle,
            val hsv: HSV = HSV()
        ) : DrawSettings() {
            data class HSV(
                val hue: Float = 0f,
                val saturation: Float = 0f,
                val value: Float = 0f
            )

            val color = with(hsv) {
                Color.hsv(hue, saturation, value)
            }
        }
    }

    sealed interface Action {
        data class SetDrawSettingsVisibility(val visible: Boolean) : Action
        data object OnNewPathStart : Action
        data class Draw(val offset: Offset) : Action
        data object OnPathEnd : Action
        sealed interface ChangePenSettings : Action {
            data class Hue(
                val hue: Float
            ) : ChangePenSettings

            data class SaturationAndValue(
                val saturation: Float,
                val value: Float
            ) : ChangePenSettings

            data class Thickness(
                val thickness: Dp
            ) : ChangePenSettings
        }

        data class OnChangeEraserSettings(
            val thickness: Dp
        ) : Action

        data object Undo : Action
        data object Redo : Action
        data object ChangeDrawingTool : Action
        data class OnNewBitmap(val bitmap: ImageBitmap) : Action
        data class SaveSketch(val context: Context) : Action
    }

    override val mutableStateFlow: MutableStateFlow<State> = MutableStateFlow(State())

    private var bitmap: ImageBitmap? = null

    fun onAction(action: Action) {
        when (action) {
            is Action.Draw -> onDraw(action.offset)
            Action.OnNewPathStart -> onNewPathStart()
            is Action.OnPathEnd -> onPathEnd()
            is Action.OnChangeEraserSettings -> onChangeEraserSettings(action)
            is Action.ChangePenSettings -> onChangePenSettings(action)
            Action.Redo -> onRedo()
            Action.Undo -> onUndo()
            Action.ChangeDrawingTool -> onChangeDrawingState()
            is Action.OnNewBitmap -> onNewBitmap(action.bitmap)
            is Action.SaveSketch -> onSaveSketch(action.context)
            is Action.SetDrawSettingsVisibility -> setDrawSettingsVisibility(action.visible)
        }
    }

    private fun onChangeDrawingState() {
        state = state.copy(
            drawing = !state.drawing
        )
    }

    private fun onUndo() {
        if (state.paths.isNotEmpty()) {
            state = state.copy(
                paths = (state.paths - state.paths.last()).toImmutableList(),
                undoPaths = (state.undoPaths + state.paths.last()).toImmutableList()
            )
        }
    }

    private fun onRedo() {
        if (state.undoPaths.isNotEmpty()) {
            state = state.copy(
                paths = (state.paths + state.undoPaths.last()).toImmutableList(),
                undoPaths = (state.undoPaths - state.undoPaths.last()).toImmutableList()
            )
        }
    }

    private fun onChangePenSettings(action: Action.ChangePenSettings) {
        mutableStateFlow.update {
            when (action) {
                is Action.ChangePenSettings.Hue -> it.copy(
                    pen = it.pen.copy(hsv = it.pen.hsv.copy(hue = action.hue))
                )

                is Action.ChangePenSettings.SaturationAndValue -> it.copy(
                    pen = it.pen.copy(
                        hsv = it.pen.hsv.copy(
                            saturation = action.saturation,
                            value = action.value
                        )
                    )
                )

                is Action.ChangePenSettings.Thickness -> it.copy(
                    pen = it.pen.copy(currentThickness = action.thickness)
                )
            }
        }
    }

    private fun onChangeEraserSettings(action: Action.OnChangeEraserSettings) {
        mutableStateFlow.update {
            it.copy(
                eraser = it.eraser.copy(currentThickness = action.thickness)
            )
        }
    }

    private fun onPathEnd() {
        state.currentPath?.let {
            state = state.copy(
                currentPath = null,
                undoPaths = persistentListOf(),
                paths = (state.paths + it).toImmutableList()
            )
        }
    }

    private fun onNewPathStart() {
        state = state.copy(
            currentPath = PathData(
                id = UUID.randomUUID().toString(),
                path = persistentListOf(),
                tool = state.currentSettings
            )
        )
    }

    private fun onDraw(offset: Offset) {
        state.currentPath?.let {
            state = state.copy(
                currentPath = it.copy(
                    path = (it.path + offset).toImmutableList()
                )
            )
        }
    }

    private fun onNewBitmap(bitmap: ImageBitmap) {
        this.bitmap = bitmap
        state = state.copy(
            saveEnabled = bitmap.isEmpty.not()
        )
    }

    private fun onSaveSketch(context: Context) {
        mutableStateFlow.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            val saveOperation = measureTimedValue {
                saveBitmapToFile.invoke(
                    context = context,
                    bitmap = bitmap ?: return@measureTimedValue null
                ).getOrNull()?.let { file ->
                    createMediaNote(
                        MediaNote.Sketch(
                            path = file.absolutePath
                        )
                    )
                }
            }

            if (saveOperation.duration < MIN_SAVE_SKETCH_DURATION) {
                delay(MIN_SAVE_SKETCH_DURATION - saveOperation.duration)
            }

            if (saveOperation.value != null) {
                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        sketchSaved = true
                    )
                }
            } else {
                onSaveError()
            }
        }
    }

    private fun onSaveError() {
        mutableStateFlow.update {
            it.copy(
                snackbarData = SnackbarData(
                    titleResId = R.string.screen_sketch__snackbar__save_error__title,
                    onDismiss = {
                        mutableStateFlow.update { it.copy(snackbarData = null) }
                    }
                )
            )
        }
    }

    private fun setDrawSettingsVisibility(isVisible: Boolean) {
        mutableStateFlow.update {
            it.copy(showDrawSettings = isVisible)
        }
    }

    private val ImageBitmap.isEmpty: Boolean
        get() {
            val emptyBitmap = createBitmap(width, height)
            return asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true).sameAs(emptyBitmap)
        }

    companion object {
        private val PEN_THICKNESS_RANGE = 2.dp..8.dp
        private val ERASER_THICKNESS_RANGE = 10.dp..40.dp
        private val MIN_SAVE_SKETCH_DURATION = 2.seconds
    }
}
