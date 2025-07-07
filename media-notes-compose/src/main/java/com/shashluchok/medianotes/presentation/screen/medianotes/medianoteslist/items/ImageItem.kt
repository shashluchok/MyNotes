package com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.shashluchok.medianotes.presentation.screen.medianotes.MediaNoteItem

private val imageMinHeight = 150.dp
private val imageMaxHeight = 300.dp

private val imageTextMinHeight = 64.dp
private val imageTextPadding = PaddingValues(
    top = 12.dp,
    start = 12.dp,
    end = 12.dp,
    bottom = 32.dp
)

@Composable
internal fun ImageItem(
    image: MediaNoteItem.Image,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = imageMinHeight, max = imageMaxHeight),
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image.path)
                    .crossfade(true)
                    .memoryCacheKey(image.path)
                    .build(),
                filterQuality = FilterQuality.High,
                contentScale = ContentScale.Crop
            ),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )
        AnimatedVisibility(
            visible = image.text.isNotEmpty()
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = imageTextMinHeight)
                    .padding(imageTextPadding),
                text = image.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
