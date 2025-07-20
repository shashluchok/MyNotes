package com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.shashluchok.medianotes.presentation.screen.medianotes.data.MediaNoteItem

private val sketchMinHeight = 150.dp
private val sketchMaxHeight = 300.dp

@Composable
internal fun SketchItem(
    sketch: MediaNoteItem.Sketch,
    modifier: Modifier = Modifier
) {
    Image(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = sketchMinHeight, max = sketchMaxHeight),
        painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(sketch.path)
                .crossfade(true)
                .memoryCacheKey(sketch.path)
                .build(),
            filterQuality = FilterQuality.High,
            contentScale = ContentScale.Crop
        ),
        contentScale = ContentScale.Crop,
        contentDescription = null
    )
}
