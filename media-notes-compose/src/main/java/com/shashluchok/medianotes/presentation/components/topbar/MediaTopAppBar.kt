package com.shashluchok.medianotes.presentation.components.topbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.shashluchok.medianotes.presentation.components.mediaicon.MediaIconButton
import com.shashluchok.medianotes.presentation.components.mediaicon.MediaIconButtonDefaults

@ExperimentalMaterial3Api
internal object MediaTopAppBarDefaults {

    @Composable
    fun topAppBarColors(
        containerColor: Color = MaterialTheme.colorScheme.surface,
        scrolledContainerColor: Color = Color.Unspecified,
        navigationIconContentColor: Color = MaterialTheme.colorScheme.primary,
        titleContentColor: Color = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor: Color = Color.Unspecified
    ) = TopAppBarDefaults.topAppBarColors(
        containerColor,
        scrolledContainerColor,
        navigationIconContentColor,
        titleContentColor,
        actionIconContentColor
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MediaTopAppBar(
    title: String,
    onNavigationIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = MediaTopAppBarDefaults.topAppBarColors(),
    navigationIconPainter: Painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.ArrowBack),
    actions: (@Composable RowScope.() -> Unit) = {}
) {
    TopAppBar(
        modifier = modifier
            .fillMaxWidth(),
        title = {
            AnimatedContent(
                targetState = title,
                transitionSpec = {
                    slideInVertically { it } togetherWith slideOutVertically { -it }
                }
            ) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.titleContentColor
                )
            }
        },
        colors = colors,
        navigationIcon = {
            AnimatedContent(
                targetState = navigationIconPainter,
                transitionSpec = {
                    slideInHorizontally { -it } togetherWith slideOutVertically { -it }
                }
            ) { painter ->
                MediaIconButton(
                    painter = painter,
                    onClick = onNavigationIconClick,
                    colors = MediaIconButtonDefaults.iconButtonColors(
                        contentColor = colors.navigationIconContentColor
                    )
                )
            }
        },
        actions = actions
    )
}
