package com.shashluchok.medianotes.presentation.modifiers.pointerinput

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.util.fastForEach
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.absoluteValue

internal fun Modifier.draggableOnLongClick(
    maxDragOffset: Float,
    onDrag: (totalDragOffset: Float) -> Unit,
    onDragStart: () -> Unit = { },
    onDragEnd: () -> Unit = { },
    onDragCancel: () -> Unit = { },
    enabled: Boolean
) = pointerInput(enabled) {
    if (enabled) {
        awaitEachGesture {
            try {
                val down = awaitFirstDown(requireUnconsumed = false)
                val drag = awaitLongPressOrCancellation(down.id)

                if (drag != null) {
                    var totalOffset = 0f
                    onDragStart.invoke()
                    if (
                        drag(drag.id) {
                            if (totalOffset.absoluteValue > maxDragOffset.absoluteValue) {
                                throw CancellationException()
                            }
                            totalOffset += it.positionChange().x
                            onDrag(totalOffset)
                            it.consume()
                        }
                    ) {
                        currentEvent.changes.fastForEach {
                            if (it.changedToUp()) it.consume()
                        }
                        onDragEnd()
                    } else {
                        onDragEnd()
                    }
                }
            } catch (c: CancellationException) {
                onDragCancel()
                throw c
            }
        }
    }
}

internal fun Modifier.detectPressAndDrag(
    onEvent: PointerInputScope.(eventOffset: Offset) -> Unit
) = pointerInput(Unit) {
    detectDragGestures(
        onDrag = { input, _ -> onEvent(input.position) }
    )
}.pointerInput(Unit) {
    detectTapGestures(
        onPress = { onEvent(it) }
    )
}
