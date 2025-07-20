package com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist.items

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shashluchok.medianotes.presentation.screen.medianotes.data.MediaNoteItem

private val textPadding = PaddingValues(
    top = 12.dp,
    start = 12.dp,
    end = 12.dp,
    bottom = 32.dp
)

private val textMinHeight = 64.dp

@Composable
internal fun TextItem(
    text: MediaNoteItem.Text,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier
            .heightIn(min = textMinHeight)
            .padding(textPadding),
        text = text.value,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}
