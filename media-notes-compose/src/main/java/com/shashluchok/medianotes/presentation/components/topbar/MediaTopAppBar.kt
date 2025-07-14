package com.shashluchok.medianotes.presentation.components.topbar

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shashluchok.medianotes.presentation.components.AnimatedVisibilityOnDisplay
import com.shashluchok.medianotes.presentation.components.mediaicon.MediaIconButton
import com.shashluchok.medianotes.presentation.components.mediaicon.MediaIconButtonDefaults
import com.shashluchok.medianotes.presentation.data.ActionIcon
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private val topBarPadding = PaddingValues(
    horizontal = 4.dp
)
private val topBarHorizontalArrangement = Arrangement.spacedBy(4.dp)
private val topAppBarHeight = 64.dp

private const val topBarIconAnimationDelayMultiplier = 50

@Composable
internal fun MediaTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ActionIcon? = null,
    actions: ImmutableList<ActionIcon> = persistentListOf()
) {
    MediaTopBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = navigationIcon,
        actions = actions
    )
}

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MediaTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: ActionIcon? = null,
    actions: ImmutableList<ActionIcon> = persistentListOf()
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(TopAppBarDefaults.windowInsets)
            .heightIn(min = topAppBarHeight)
            .padding(topBarPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = topBarHorizontalArrangement
    ) {
        navigationIcon?.let {
            AnimatedContent(
                targetState = it.painter,
                transitionSpec = {
                    slideInHorizontally { -it } togetherWith slideOutVertically { -it }
                }
            ) { painter ->
                MediaIconButton(
                    painter = painter,
                    onClick = it.onClick,
                    colors = it.colors ?: MediaIconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            title()
        }
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
}
