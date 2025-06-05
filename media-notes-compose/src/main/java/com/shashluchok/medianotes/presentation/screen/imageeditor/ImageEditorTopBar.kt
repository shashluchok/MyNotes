package com.shashluchok.medianotes.presentation.screen.imageeditor

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.shashluchok.medianotes.presentation.components.AnimatedVisibilityOnDisplay
import com.shashluchok.medianotes.presentation.components.mediaicon.MediaIconButton
import com.shashluchok.medianotes.presentation.components.topbar.MediaTopAppBar
import com.shashluchok.medianotes.presentation.components.topbar.MediaTopAppBarDefaults
import kotlinx.collections.immutable.ImmutableList

private val topBarBrush = Brush.verticalGradient(
    colorStops = arrayOf(
        0f to Color.Black,
        1f to Color.Transparent
    )
)

private val topBarPadding = PaddingValues(bottom = 24.dp)
private const val topBarIconAnimationDelayMultiplier = 50

internal data class ActionIcon(
    val painter: Painter,
    val onClick: () -> Unit,
    val colors: IconButtonColors? = null,
    val contentDescription: String? = null,
    val enabled: Boolean = true
)

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImageViewerTopBar(
    navIcon: ActionIcon,
    title: String,
    actions: ImmutableList<ActionIcon>,
    modifier: Modifier = Modifier
) {
    MediaTopAppBar(
        modifier = modifier
            .fillMaxWidth()
            .padding(topBarPadding)
            .background(
                brush = topBarBrush
            )
            .padding(topBarPadding),
        title = title,
        colors = MediaTopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        ),
        onNavigationIconClick = navIcon.onClick,
        navigationIconPainter = navIcon.painter,
        actions = {
            AnimatedContent(
                targetState = actions.size,
                transitionSpec = {
                    slideInHorizontally(tween()) { it } togetherWith slideOutHorizontally { it }
                }
            ) { _ ->
                Row {
                    actions.onEachIndexed { index, action ->
                        AnimatedVisibilityOnDisplay(
                            enter = slideInHorizontally(
                                animationSpec = tween(
                                    delayMillis = (actions.lastIndex - index) * topBarIconAnimationDelayMultiplier
                                )
                            ) { it }
                        ) {
                            MediaIconButton(
                                painter = action.painter,
                                colors = action.colors ?: IconButtonDefaults.iconButtonColors(),
                                onClick = action.onClick,
                                contentDescription = action.contentDescription,
                                enabled = action.enabled
                            )
                        }
                    }
                }
            }
        }
    )
}
