package com.shashluchok.medianotes.presentation.components.tooltip

import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TooltipDefaults.rememberRichTooltipPositionProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.shashluchok.medianotes.presentation.utils.toDp

private val tooltipSpacingBetweenTooltipAndAnchor = 4.dp
private val tooltipMinHeight = 24.dp
private val tooltipMinWidth = 40.dp
private val toolTipCaretSize = DpSize(20.dp, 8.dp)
private val toolTipShape = RoundedCornerShape(8.dp)

private const val tooltipEnterAnimationDuration = 250
private const val tooltipExitAnimationDuration = 50
private const val tooltipMinScale = 0.5f

private const val tooltipInfiniteAnimationLabel = "tooltipInfiniteAnimation"
private const val tooltipBounceAnimationLabel = "tooltipBounceAnimation"
private const val tooltipBounceAnimationDuration = 1000
private const val tooltipBounceOffset = 20f

@Composable
@ExperimentalMaterial3Api
internal fun TooltipContainer(
    toolTipVisible: Boolean,
    onToolTipDismiss: () -> Unit,
    tooltip: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    animate: Boolean = false,
    caretSize: DpSize = toolTipCaretSize,
    shape: Shape = toolTipShape,
    containerColor: Color = MaterialTheme.colorScheme.inverseSurface,
    content: @Composable () -> Unit
) {
    val popupPositionProvider = rememberRichTooltipPositionProvider()

    var anchorBounds by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val scale by animateFloatAsState(
        targetValue = if (toolTipVisible) 1f else tooltipMinScale,
        animationSpec = if (toolTipVisible) {
            tween(durationMillis = tooltipEnterAnimationDuration)
        } else {
            tween(durationMillis = tooltipExitAnimationDuration)
        }
    )

    DisposableEffect(Unit) {
        onDispose { onToolTipDismiss() }
    }

    Box {
        if (toolTipVisible) {
            Popup(
                popupPositionProvider = popupPositionProvider,
                onDismissRequest = {
                    onToolTipDismiss()
                }
            ) {
                Surface(
                    modifier = modifier
                        .scale(scale)
                        .toolTipCaret(
                            caretSize = caretSize,
                            color = containerColor,
                            anchorLayoutCoordinates = anchorBounds,
                            animate = animate
                        ),
                    shape = shape,
                    color = containerColor
                ) {
                    Box(
                        modifier = Modifier
                            .sizeIn(
                                minWidth = tooltipMinWidth,
                                minHeight = tooltipMinHeight
                            )
                    ) {
                        tooltip()
                    }
                }
            }
        }
        Box(
            modifier = Modifier.onGloballyPositioned {
                anchorBounds = it
            }
        ) {
            content()
        }
    }
}

private fun Modifier.toolTipCaret(
    caretSize: DpSize,
    color: Color,
    anchorLayoutCoordinates: LayoutCoordinates?,
    animate: Boolean = false
) = composed {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val infiniteTransition = rememberInfiniteTransition(label = tooltipInfiniteAnimationLabel)

    var targetOffset by remember {
        mutableFloatStateOf(0f)
    }

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animate) {
            targetOffset
        } else {
            0f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = tooltipBounceAnimationDuration,
                easing = EaseInBounce
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = tooltipBounceAnimationLabel
    )

    offset(y = progress.toDp()).drawWithCache {
        val path = Path()
        anchorLayoutCoordinates?.let {
            with(density) {
                val caretHeightPx = caretSize.height.roundToPx()
                val caretWidthPx = caretSize.width.roundToPx()
                val screenWidthPx = configuration.screenWidthDp.dp.roundToPx()
                val tooltipAnchorSpacing = tooltipSpacingBetweenTooltipAndAnchor.roundToPx()

                val anchorBounds = it.boundsInWindow()
                val anchorLeft = anchorBounds.left
                val anchorRight = anchorBounds.right
                val anchorTop = anchorBounds.top
                val anchorMid = (anchorRight + anchorLeft) / 2
                val anchorWidth = anchorRight - anchorLeft
                val tooltipWidth = this@drawWithCache.size.width
                val tooltipHeight = this@drawWithCache.size.height
                val isCaretTop = anchorTop - tooltipHeight - tooltipAnchorSpacing < 0
                val caretY: Float
                if (isCaretTop) {
                    targetOffset = tooltipBounceOffset
                    caretY = 1f
                } else {
                    targetOffset = -tooltipBounceOffset
                    caretY = tooltipHeight - 1f
                }

                var preferredPosition = Offset(anchorMid - anchorLeft, caretY)
                if (anchorLeft + tooltipWidth > screenWidthPx) {
                    preferredPosition = Offset(anchorMid - (anchorRight - tooltipWidth), caretY)
                    if (anchorRight - tooltipWidth < 0) {
                        preferredPosition = if (anchorLeft - tooltipWidth / 2 + anchorWidth / 2 <= 0) {
                            Offset(anchorMid, caretY)
                        } else if (anchorRight + tooltipWidth / 2 - anchorWidth / 2 >= screenWidthPx) {
                            val anchorMidFromRightScreenEdge = screenWidthPx - anchorMid
                            val caretX = tooltipWidth - anchorMidFromRightScreenEdge
                            Offset(caretX, caretY)
                        } else {
                            Offset(tooltipWidth / 2, caretY)
                        }
                    }
                }
                val position = preferredPosition

                if (isCaretTop) {
                    path.apply {
                        moveTo(x = position.x, y = position.y)
                        moveTo(x = position.x - caretWidthPx / 2, y = position.y)
                        quadraticTo(
                            x1 = position.x - caretWidthPx / 2 * 0.5f,
                            y1 = position.y - caretHeightPx * 0.25f,
                            x2 = position.x - caretWidthPx / 2 * 0.2f,
                            y2 = position.y - caretHeightPx * 0.75f
                        )
                        quadraticTo(
                            x1 = position.x,
                            y1 = position.y - caretHeightPx,
                            x2 = position.x + caretWidthPx / 2 * 0.2f,
                            y2 = position.y - caretHeightPx * 0.75f
                        )
                        quadraticTo(
                            x1 = position.x + caretWidthPx / 2 * 0.5f,
                            y1 = position.y - caretHeightPx * 0.25f,
                            x2 = position.x + caretWidthPx / 2,
                            y2 = position.y
                        )
                        close()
                    }
                } else {
                    path.apply {
                        moveTo(x = position.x - caretWidthPx / 2, y = position.y)
                        quadraticTo(
                            x1 = position.x - caretWidthPx / 2 * 0.5f,
                            y1 = position.y + caretHeightPx * 0.25f,
                            x2 = position.x - caretWidthPx / 2 * 0.2f,
                            y2 = position.y + caretHeightPx * 0.75f
                        )
                        quadraticTo(
                            x1 = position.x,
                            y1 = position.y + caretHeightPx,
                            x2 = position.x + caretWidthPx / 2 * 0.2f,
                            y2 = position.y + caretHeightPx * 0.75f
                        )
                        quadraticTo(
                            x1 = position.x + caretWidthPx / 2 * 0.5f,
                            y1 = position.y + caretHeightPx * 0.25f,
                            x2 = position.x + caretWidthPx / 2,
                            y2 = position.y
                        )
                        close()
                    }
                }
            }
        }

        onDrawBehind {
            if (anchorLayoutCoordinates != null) {
                drawPath(path = path, color = color)
            }
        }
    }
}
