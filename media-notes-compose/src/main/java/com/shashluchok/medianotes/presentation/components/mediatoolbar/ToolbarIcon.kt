package com.shashluchok.medianotes.presentation.components.mediatoolbar

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.shashluchok.medianotes.presentation.components.CustomRippleColor

private val toolbarIconSize = 24.dp

@Composable
internal fun ToolbarIcon(
    painter: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color? = null
) {
    CustomRippleColor(
        color = MaterialTheme.colorScheme.tertiary
    ) {
        IconButton(
            modifier = modifier.testTag(tag = ToolbarIcon.Tag.root),
            onClick = onClick
        ) {
            Icon(
                modifier = Modifier.size(toolbarIconSize),
                painter = painter,
                tint = tint ?: MaterialTheme.colorScheme.primary,
                contentDescription = contentDescription
            )
        }
    }
}

object ToolbarIcon {
    object Tag {
        const val root = "ToolbarIcon"
    }
}
