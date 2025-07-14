package com.shashluchok.medianotes.presentation.screen.imageeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shashluchok.medianotes.presentation.components.topbar.MediaTopBar
import com.shashluchok.medianotes.presentation.data.ActionIcon
import kotlinx.collections.immutable.ImmutableList

private val topBarBrush = Brush.verticalGradient(
    colorStops = arrayOf(
        0f to Color.Black,
        1f to Color.Transparent
    )
)

private val topBarPadding = PaddingValues(bottom = 24.dp)

@Composable
internal fun ImageViewerTopBar(
    navIcon: ActionIcon,
    title: @Composable () -> Unit,
    actions: ImmutableList<ActionIcon>,
    modifier: Modifier = Modifier
) {
    MediaTopBar(
        modifier = modifier
            .fillMaxWidth()
            .padding(topBarPadding)
            .background(brush = topBarBrush)
            .padding(topBarPadding),
        title = title,
        navigationIcon = navIcon,
        actions = actions
    )
}
