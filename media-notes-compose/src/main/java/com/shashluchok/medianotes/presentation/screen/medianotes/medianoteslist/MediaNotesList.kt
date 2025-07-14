package com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shashluchok.medianotes.presentation.MediaImage
import com.shashluchok.medianotes.presentation.modifiers.scrollbars.ScrollbarConfig
import com.shashluchok.medianotes.presentation.modifiers.scrollbars.scrollbar
import com.shashluchok.medianotes.presentation.screen.medianotes.MediaNoteItem
import com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist.items.ImageItem
import com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist.items.MediaNoteItemBox
import com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist.items.SketchItem
import com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist.items.TextItem
import com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist.items.VoiceItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

private val scrollbarPadding = PaddingValues(end = 8.dp)

private val emptyStatePadding = PaddingValues(horizontal = 16.dp)

private const val listAutoScrollDuration = 500

private val scrollValuePerListItem = 350.dp

@Composable
internal fun MediaNotesList(
    listState: LazyListState,
    notes: ImmutableList<MediaNoteItem>,
    selectedNotes: ImmutableList<MediaNoteItem>,
    onSelect: (MediaNoteItem) -> Unit,
    onOpenImage: (MediaImage) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MediaNotesListViewModel = viewModel()
) {
    val state = viewModel.stateFlow.collectAsState().value

    val lifecycleOwner = LocalLifecycleOwner.current

    val density = LocalDensity.current

    var previousNotesSize by remember { mutableIntStateOf(notes.size) }

    val notVisibleBottomItemsSize by remember {
        derivedStateOf {
            val allItemsLastIndex = listState.layoutInfo.totalItemsCount - 1
            if (allItemsLastIndex >= 0) {
                val visibleItemsLastIndex = listState.layoutInfo.visibleItemsInfo.last().index
                allItemsLastIndex - visibleItemsLastIndex
            } else {
                0
            }
        }
    }

    LaunchedEffect(notes) {
        if (notes.size > previousNotesSize) {
            with(density) {
                listState.animateScrollBy(
                    value = notVisibleBottomItemsSize * scrollValuePerListItem.toPx(),
                    animationSpec = tween(
                        durationMillis = listAutoScrollDuration,
                        easing = LinearEasing
                    )
                )
            }
        }
        previousNotesSize = notes.size
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.onAction(MediaNotesListViewModel.Action.OnAppBackground)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AnimatedWavesContainer(
        modifier = modifier,
        wavesVisible = notes.isEmpty()
    ) {
        MediaNotesList(
            modifier = Modifier.fillMaxSize(),
            mediaNotes = notes,
            selectedNotes = selectedNotes,
            onSelect = onSelect,
            lazyListState = listState,
            onOpenImage = onOpenImage,
            playVoiceInfo = state.playingVoiceInfo,
            onAction = viewModel::onAction
        )
    }
}

@Composable
private fun MediaNotesList(
    mediaNotes: ImmutableList<MediaNoteItem>,
    lazyListState: LazyListState,
    selectedNotes: ImmutableList<MediaNoteItem>,
    onOpenImage: (MediaImage) -> Unit,
    onSelect: (noteItem: MediaNoteItem) -> Unit,
    playVoiceInfo: MediaNotesListViewModel.PlayVoiceInfo?,
    onAction: (MediaNotesListViewModel.Action) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    AnimatedContent(
        modifier = modifier,
        targetState = mediaNotes.isEmpty(),
        transitionSpec = {
            if (targetState) {
                fadeIn()
            } else {
                slideInVertically { -it }
            } togetherWith fadeOut()
        }
    ) { notesEmpty ->
        if (notesEmpty) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(emptyStatePadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .scrollbar(
                        state = lazyListState,
                        config = ScrollbarConfig(padding = scrollbarPadding)
                    ),
                state = lazyListState
            ) {
                items(
                    items = mediaNotes,
                    key = { it.id }
                ) { mediaNote ->
                    MediaNoteItemBox(
                        modifier = Modifier
                            .animateItem()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        onSelect(mediaNote)
                                    },
                                    onTap = {
                                        when (mediaNote) {
                                            is MediaNoteItem.Image -> {
                                                scope.launch {
                                                    lazyListState.scrollToItemIfNotVisible(mediaNote.id)
                                                    onOpenImage(
                                                        MediaImage(
                                                            id = mediaNote.id,
                                                            path = mediaNote.path,
                                                            type = MediaImage.Type.IMAGE_NOTE
                                                        )
                                                    )
                                                }
                                            }

                                            is MediaNoteItem.Sketch -> {
                                                scope.launch {
                                                    lazyListState.scrollToItemIfNotVisible(mediaNote.id)
                                                    onOpenImage(
                                                        MediaImage(
                                                            id = mediaNote.id,
                                                            path = mediaNote.path,
                                                            type = MediaImage.Type.SKETCH_NOTE
                                                        )
                                                    )
                                                }
                                            }

                                            is MediaNoteItem.Text,
                                            is MediaNoteItem.Voice -> Unit
                                        }
                                    }
                                )
                            },
                        updatedTime = mediaNote.updatedAt,
                        note = mediaNote,
                        isSelecting = selectedNotes.isNotEmpty(),
                        onSelect = onSelect,
                        selected = selectedNotes.contains(mediaNote)

                    ) { mediaNoteItem ->
                        when (mediaNoteItem) {
                            is MediaNoteItem.Text -> TextItem(text = mediaNoteItem)
                            is MediaNoteItem.Image -> ImageItem(
                                image = mediaNoteItem
                            )

                            is MediaNoteItem.Sketch -> SketchItem(
                                sketch = mediaNoteItem
                            )

                            is MediaNoteItem.Voice -> {
                                VoiceItem(
                                    voice = mediaNoteItem,
                                    isPlaying = playVoiceInfo?.let {
                                        it.mediaNote.id == mediaNoteItem.id && it.paused.not()
                                    } ?: false,
                                    playProgress = playVoiceInfo?.let {
                                        if (it.mediaNote.id == mediaNoteItem.id) it.progress else 1f
                                    } ?: 1f,
                                    onPlay = {
                                        onAction(MediaNotesListViewModel.Action.OnPlayClick(it))
                                    },
                                    onSeek = { mediaNote, progress ->
                                        onAction(MediaNotesListViewModel.Action.OnSeek(mediaNote, progress))
                                    },
                                    onSeekStart = { mediaNote, progress ->
                                        onAction(
                                            MediaNotesListViewModel.Action.OnSeekStart(
                                                mediaNote,
                                                progress
                                            )
                                        )
                                    },
                                    onSeekEnd = {
                                        onAction(
                                            MediaNotesListViewModel.Action.OnSeekEnd(it)
                                        )
                                    },
                                    duration = playVoiceInfo?.let {
                                        if (it.mediaNote.id == mediaNoteItem.id) {
                                            it.remainingDuration
                                        } else {
                                            mediaNoteItem.duration
                                        }
                                    } ?: mediaNoteItem.duration,
                                    seekEnabled = playVoiceInfo?.mediaNote == mediaNoteItem
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun LazyListState.scrollToItemIfNotVisible(itemKey: String) {
    val item = layoutInfo.visibleItemsInfo.find {
        it.key == itemKey
    } ?: return
    if (item.size > layoutInfo.viewportSize.height) {
        return
    }
    val itemOffset = item.offset
    val endOffset = layoutInfo.viewportEndOffset - (itemOffset + item.size)

    if (itemOffset < 0) {
        animateScrollBy(itemOffset.toFloat())
    } else if (endOffset < 0) {
        animateScrollBy(-endOffset.toFloat())
    }
}
