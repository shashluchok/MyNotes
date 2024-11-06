package com.shashluchok.medianotes.presentation.modifiers.scrollbars

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.LayoutDirection

internal abstract class ScrollbarDrawer {
    abstract val state: ScrollableState
    abstract val orientation: Orientation

    fun DrawScope.drawScrollbar(
        color: Color,
        alpha: Float,
        dimensions: ScrollbarDimensions
    ) {
        if (shouldDraw()) {
            drawRoundRect(
                color = color,
                topLeft = topLeft(dimensions),
                size = size(dimensions),
                cornerRadius = dimensions.cornerRadius,
                alpha = alpha
            )
        }
    }

    protected open fun canvasLength(drawScope: DrawScope): Float = drawScope.run {
        return when (orientation) {
            Orientation.Vertical -> size.height
            Orientation.Horizontal -> size.width
        }
    }

    private fun DrawScope.shouldDraw(): Boolean {
        return totalItemsSize() != 0f && thumbSize() < canvasLength(this)
    }

    private fun DrawScope.topLeft(dimensions: ScrollbarDimensions): Offset {
        return Offset(
            x = x(dimensions.startPadding, dimensions.endPadding, dimensions.thickness),
            y = y(dimensions.topPadding, dimensions.bottomPadding, dimensions.thickness)
        )
    }

    private fun DrawScope.size(dimensions: ScrollbarDimensions): Size {
        val indicatorOffset = when (orientation) {
            Orientation.Vertical -> dimensions.topPadding + dimensions.bottomPadding
            Orientation.Horizontal -> dimensions.startPadding + dimensions.endPadding
        }
        val indicatorLength = thumbSize() - indicatorOffset

        return when (orientation) {
            Orientation.Vertical -> Size(dimensions.thickness, indicatorLength)
            Orientation.Horizontal -> Size(indicatorLength, dimensions.thickness)
        }
    }

    private fun DrawScope.viewportAxisLength(): Float {
        return when (orientation) {
            Orientation.Vertical -> size.height
            Orientation.Horizontal -> size.width
        }
    }

    private fun DrawScope.viewPortCrossAxisLength(): Float {
        return when (orientation) {
            Orientation.Vertical -> size.width
            Orientation.Horizontal -> size.height
        }
    }

    private fun DrawScope.x(
        startPaddingPx: Float,
        endPaddingPx: Float,
        thicknessPx: Float
    ): Float {
        return when (orientation) {
            Orientation.Vertical -> when (layoutDirection) {
                LayoutDirection.Ltr -> viewPortCrossAxisLength() - thicknessPx - endPaddingPx
                LayoutDirection.Rtl -> startPaddingPx
            }

            Orientation.Horizontal -> when (layoutDirection) {
                LayoutDirection.Ltr -> startOffset() + startPaddingPx
                LayoutDirection.Rtl -> viewportAxisLength() - startOffset() - thumbSize() - endPaddingPx
            }
        }
    }

    private fun DrawScope.y(
        topPaddingPx: Float,
        bottomPaddingPx: Float,
        thicknessPx: Float
    ): Float {
        return when (orientation) {
            Orientation.Vertical -> startOffset() + topPaddingPx
            Orientation.Horizontal -> viewPortCrossAxisLength() - thicknessPx - bottomPaddingPx
        }
    }

    private fun DrawScope.startOffset(): Float = currentOffset() / totalItemsSize() * canvasLength(this)

    abstract fun DrawScope.thumbSize(): Float
    abstract fun DrawScope.currentOffset(): Float
    abstract fun DrawScope.totalItemsSize(): Float
}

internal class DefaultScrollbarDrawer(
    override val state: ScrollState,
    override val orientation: Orientation
) : ScrollbarDrawer() {
    override fun DrawScope.thumbSize(): Float {
        return canvasLength(this) / totalItemsSize() * canvasLength(this)
    }

    override fun DrawScope.currentOffset(): Float {
        return state.value.toFloat()
    }

    override fun DrawScope.totalItemsSize(): Float {
        return canvasLength(this) + state.maxValue.toFloat()
    }
}

internal class LazyListScrollbarDrawer(
    override val state: LazyListState
) : ScrollbarDrawer() {
    override fun canvasLength(drawScope: DrawScope): Float {
        return super.canvasLength(drawScope) - state.layoutInfo.afterContentPadding
    }

    override val orientation: Orientation
        get() = state.layoutInfo.orientation

    private val viewportSize get() = with(state.layoutInfo) {
        viewportEndOffset - viewportStartOffset - afterContentPadding
    }

    private val estimatedItemSize: Float
        get() {
            val totalVisibleItemsSize = state.layoutInfo.visibleItemsInfo.sumOf { it.size }.toFloat()
            return totalVisibleItemsSize / state.layoutInfo.visibleItemsInfo.size
        }

    override fun DrawScope.thumbSize(): Float {
        return viewportSize / totalItemsSize() * canvasLength(this)
    }

    override fun DrawScope.currentOffset(): Float {
        return estimatedItemSize * state.firstVisibleItemIndex + state.firstVisibleItemScrollOffset
    }

    override fun DrawScope.totalItemsSize(): Float {
        return estimatedItemSize * state.layoutInfo.totalItemsCount
    }
}

internal class LazyGridScrollbarDrawer(
    override val state: LazyGridState,
    override val orientation: Orientation,
    private val spans: Int
) : ScrollbarDrawer() {
    override fun canvasLength(drawScope: DrawScope): Float {
        return super.canvasLength(drawScope) - state.layoutInfo.afterContentPadding
    }

    private val viewportSize get() = with(state.layoutInfo) {
        viewportEndOffset - viewportStartOffset - afterContentPadding
    }

    private val visibleSpans get() = (state.layoutInfo.visibleItemsInfo.size + spans - 1) / spans
    private val totalSpans get() = (state.layoutInfo.totalItemsCount + spans - 1) / spans

    override fun DrawScope.thumbSize(): Float {
        return viewportSize / totalItemsSize() * canvasLength(this)
    }

    override fun DrawScope.currentOffset(): Float {
        return estimatedItemSize() * state.firstVisibleItemIndex / spans + state.firstVisibleItemScrollOffset
    }

    override fun DrawScope.totalItemsSize(): Float {
        return estimatedItemSize() * totalSpans
    }

    private fun estimatedItemSize(): Float {
        val items = state.layoutInfo.visibleItemsInfo
        var totalVisibleItemsSize = 0f
        for (i in 0 until visibleSpans) {
            totalVisibleItemsSize += if (orientation == Orientation.Vertical) {
                items[i * spans].size.height
            } else {
                items[i * spans].size.width
            }
        }
        return totalVisibleItemsSize / visibleSpans
    }
}

internal data class ScrollbarDimensions(
    val startPadding: Float,
    val endPadding: Float,
    val topPadding: Float,
    val bottomPadding: Float,
    val thickness: Float,
    val cornerRadius: CornerRadius
) {
    companion object {
        fun ScrollbarConfig.toDimensions(drawScope: DrawScope): ScrollbarDimensions = drawScope.run {
            val thicknessPx = thickness.toPx()

            val topPadding = padding.calculateTopPadding().toPx()
            val bottomPadding = padding.calculateBottomPadding().toPx()
            val startPadding = padding.calculateStartPadding(layoutDirection).toPx()
            val endPadding = padding.calculateEndPadding(layoutDirection).toPx()

            val cornerRadius = CornerRadius(
                x = thickness.toPx() / 2,
                y = thickness.toPx() / 2
            )

            return ScrollbarDimensions(
                startPadding = startPadding,
                endPadding = endPadding,
                topPadding = topPadding,
                bottomPadding = bottomPadding,
                thickness = thicknessPx,
                cornerRadius = cornerRadius
            )
        }
    }
}
