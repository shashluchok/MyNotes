package com.shashluchok.medianotes.presentation.screen.medianotes.mediatoolbar

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shashluchok.medianotes.R

private val swipeToCancelIconSize = 24.dp

@Composable
internal fun SwipeToCancelItem(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        Icon(
            modifier = Modifier.size(swipeToCancelIconSize),
            painter = rememberVectorPainter(Icons.Outlined.ChevronLeft),
            tint = MaterialTheme.colorScheme.error,
            contentDescription = null
        )
        Text(
            text = stringResource(id = R.string.screen_media_notes__toolbar__cancel_recording__title),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
