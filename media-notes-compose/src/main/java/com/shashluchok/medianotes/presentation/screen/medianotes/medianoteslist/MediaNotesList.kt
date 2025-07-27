package com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shashluchok.medianotes.presentation.data.MediaImage
import com.shashluchok.medianotes.presentation.modifiers.scrollbars.ScrollbarConfig
import com.shashluchok.medianotes.presentation.modifiers.scrollbars.scrollbar
import com.shashluchok.medianotes.presentation.screen.medianotes.data.MediaNoteItem
import com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist.items.ImageItem
import com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist.items.MediaNoteItemBox
import com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist.items.SketchItem
import com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist.items.TextItem
import com.shashluchok.medianotes.presentation.screen.medianotes.medianoteslist.items.VoiceItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private val scrollbarPadding = PaddingValues(end = 8.dp)

private val emptyStatePadding = PaddingValues(horizontal = 16.dp)

private const val listAutoScrollDuration = 500

private val scrollValuePerListItem = 350.dp

private val dateHeaderOuterPadding = PaddingValues(
    vertical = 4.dp
)
private val dateHeaderInnerPadding = PaddingValues(
    vertical = 4.dp,
    horizontal = 6.dp
)
private const val dateHeaderBackgroundAlpha = 0.65f
private val dateHeaderShape = RoundedCornerShape(12.dp)

@Composable
internal fun MediaNotesList(
    listState: LazyListState,
    selectedNotes: ImmutableList<MediaNoteItem>,
    editingNote: MediaNoteItem?,
    onSelect: (MediaNoteItem) -> Unit,
    onOpenImage: (MediaImage) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MediaNotesListViewModel = koinViewModel()
) {
    val state = viewModel.stateFlow.collectAsState().value

    val lifecycleOwner = LocalLifecycleOwner.current

    val density = LocalDensity.current

    var previousNotesSize by remember { mutableIntStateOf(state.notes.size) }

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

    LaunchedEffect(state.notes) {
        if (state.notes.size > previousNotesSize) {
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
        previousNotesSize = state.notes.size
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
        wavesVisible = state.notes.isEmpty()
    ) {
        MediaNotesList(
            modifier = Modifier.fillMaxSize(),
            mediaNotes = state.notes,
            selectedNotes = selectedNotes,
            onSelect = onSelect,
            lazyListState = listState,
            onOpenImage = onOpenImage,
            playVoiceInfo = state.playingVoiceInfo,
            onAction = viewModel::onAction,
            editingNote = editingNote
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaNotesList(
    mediaNotes: ImmutableList<MediaNoteItem>,
    editingNote: MediaNoteItem?,
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
                horizontalAlignment = Alignment.CenterHorizontally,
                state = lazyListState
            ) {
                mediaNotes.groupBy {
                    it.createdTimeStamp.dayAndMonth
                }.onEach {
                    stickyHeader {
                        DateHeader(date = it.key)
                    }
                    itemsIndexed(
                        items = it.value,
                        key = { _, item -> item.id }
                    ) { _, mediaNote ->
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
                            timeStamp = mediaNote.createdTimeStamp.hourAndMinute,
                            note = mediaNote,
                            isSelecting = selectedNotes.isNotEmpty(),
                            onSelect = onSelect,
                            selected = selectedNotes.contains(mediaNote),
                            editing = editingNote?.id == mediaNote.id

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
}

@Composable
private fun DateHeader(
    date: String,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier
            .padding(dateHeaderOuterPadding)
            .background(
                shape = dateHeaderShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = dateHeaderBackgroundAlpha)
            )
            .padding(dateHeaderInnerPadding),
        text = date,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
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
