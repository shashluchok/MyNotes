package com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.ScaleToBounds
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.shashluchok.medianotes.presentation.SharedTransitionUtils.SharedElementType
import com.shashluchok.medianotes.presentation.SharedTransitionUtils.getKeyByElementType
import com.shashluchok.medianotes.presentation.components.LocalIsCompact
import com.shashluchok.medianotes.presentation.components.LocalNavAnimatedVisibilityScope
import com.shashluchok.medianotes.presentation.components.LocalSharedTransitionScope
import com.shashluchok.medianotes.presentation.screen.medianotes.data.MediaNoteItem

private val containerExpandedWidth = 420.dp
private val containerPadding = PaddingValues(vertical = 4.dp, horizontal = 16.dp)
private val containerShape = RoundedCornerShape(12.dp)
private val containerBorderWidth = 1.dp
private const val containerBorderAlpha = 0.35f

private val selectionCheckboxSize = 20.dp
private val selectionContentOffset = 36.dp
private const val selectionColorAlpha = 0.12f

private val timeStampShape = RoundedCornerShape(topStart = 12.dp, bottomEnd = 12.dp)
private val timeStampPadding = PaddingValues(horizontal = 12.dp)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun MediaNoteItemBox(
    timeStamp: String,
    note: MediaNoteItem,
    editing: Boolean,
    selected: Boolean,
    isSelecting: Boolean,
    onSelect: (MediaNoteItem) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(MediaNoteItem) -> Unit
) {
    val widthModifier = if (LocalIsCompact.current) {
        Modifier.fillMaxWidth()
    } else {
        Modifier.width(containerExpandedWidth)
    }
    with(LocalSharedTransitionScope.current) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(intrinsicSize = IntrinsicSize.Max)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(containerPadding)
            ) {
                AnimatedVisibility(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .requiredSize(selectionCheckboxSize),
                    visible = isSelecting,
                    enter = fadeIn() + slideInHorizontally { -it },
                    exit = fadeOut() + slideOutHorizontally { -it }
                ) {
                    Checkbox(
                        modifier = Modifier,
                        checked = selected,
                        onCheckedChange = { _ ->
                            onSelect(note)
                        }
                    )
                }

                val contentOffsetX = animateDpAsState(
                    targetValue = if (isSelecting) selectionContentOffset else 0.dp,
                    animationSpec = tween()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = contentOffsetX.value)
                        .sharedBounds(
                            rememberSharedContentState(
                                key = getKeyByElementType(
                                    SharedElementType.MediaNote(note)
                                )
                            ),
                            resizeMode = ScaleToBounds(ContentScale.None),
                            animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
                        )
                        .clip(containerShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = containerBorderWidth,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = containerBorderAlpha),
                            shape = containerShape
                        )
                        .then(widthModifier)
                ) {
                    content(note)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = timeStampShape
                            )
                            .padding(timeStampPadding)
                    ) {
                        Text(
                            text = timeStamp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            if (isSelecting || editing) {
                val background = animateColorAsState(
                    targetValue = when {
                        selected || editing -> MaterialTheme.colorScheme.primary.copy(alpha = selectionColorAlpha)
                        else -> Color.Unspecified
                    },
                    animationSpec = tween()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = background.value)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onSelect(note)
                            }
                        )
                )
            }
        }
    }
}
