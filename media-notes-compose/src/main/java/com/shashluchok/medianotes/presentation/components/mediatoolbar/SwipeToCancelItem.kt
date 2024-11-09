package com.shashluchok.medianotes.presentation.components.mediatoolbar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shashluchok.medianotes.R

private val swipeToCancelIconSize = 24.dp

@Composable
internal fun SwipeToCancelItem(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.testTag(tag = SwipeToCancelItem.Tag.root)
    ) {
        Icon(
            modifier = Modifier
                .testTag(tag = SwipeToCancelItem.Tag.icon)
                .size(swipeToCancelIconSize),
            painter = rememberVectorPainter(Icons.Outlined.ChevronLeft),
            tint = MaterialTheme.colorScheme.error,
            contentDescription = null
        )
        Text(
            modifier = Modifier.testTag(tag = SwipeToCancelItem.Tag.text),
            text = stringResource(id = R.string.media_notes_toolbar__voice_recording__cancel__title),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

object SwipeToCancelItem {
    object Tag {
        const val root = "SwipeToCancelItem"
        const val icon = "$root.Icon"
        const val text = "$root.Text"
    }
}
