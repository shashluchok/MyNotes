package com.shashluchok.medianotes.presentation.screen.medianotes.data

import android.content.res.Resources
import com.shashluchok.audiorecorder.audio.FileDataSource
import com.shashluchok.audiorecorder.audio.codec.AudioDecoder
import com.shashluchok.medianotes.R
import com.shashluchok.medianotes.data.MediaNote
import com.shashluchok.medianotes.presentation.utils.toAudioDisplayString
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import java.io.File

internal sealed interface MediaNoteItem {

    data class CreatedTimeStamp(
        val hourAndMinute: String,
        val dayAndMonth: String
    )

    sealed interface WithText : MediaNoteItem {
        val text: String
    }

    val id: String
    val createdTimeStamp: CreatedTimeStamp

    data class Voice(
        override val id: String,
        override val createdTimeStamp: CreatedTimeStamp,
        val path: String,
        val peaks: ImmutableList<Float>,
        val durationMillis: Long,
        val duration: String
    ) : MediaNoteItem

    data class Image(
        override val id: String,
        override val createdTimeStamp: CreatedTimeStamp,
        val path: String,
        override val text: String = ""
    ) : WithText

    data class Sketch(
        override val id: String,
        override val createdTimeStamp: CreatedTimeStamp,
        val path: String
    ) : MediaNoteItem

    data class Text(
        override val id: String,
        override val createdTimeStamp: CreatedTimeStamp,
        override val text: String
    ) : WithText
}

internal suspend fun MediaNote.toMediaNoteItem(
    decoder: AudioDecoder,
    resources: Resources
) = when (this) {
    is MediaNote.Image -> MediaNoteItem.Image(
        id = id,
        createdTimeStamp = createdAt.toTimeStamp(resources),
        path = path,
        text = text
    )

    is MediaNote.Sketch -> MediaNoteItem.Sketch(
        id = id,
        createdTimeStamp = createdAt.toTimeStamp(resources),
        path = path
    )

    is MediaNote.Text -> MediaNoteItem.Text(
        id = id,
        createdTimeStamp = createdAt.toTimeStamp(resources),
        text = text
    )

    is MediaNote.Voice -> {
        val (peaks, duration) = decoder.calculateVolumeLevelsAndDuration(
            FileDataSource(file = File(path))
        )
        MediaNoteItem.Voice(
            id = id,
            createdTimeStamp = createdAt.toTimeStamp(resources),
            path = path,
            peaks = peaks,
            duration = duration.toAudioDisplayString(),
            durationMillis = duration.inWholeMilliseconds
        )
    }
}

private fun Instant.toTimeStamp(
    resources: Resources
): MediaNoteItem.CreatedTimeStamp {
    val monthNames = MonthNames(
        january = resources.getString(R.string.screen_media_notes__date_header__january__title),
        february = resources.getString(R.string.screen_media_notes__date_header__february__title),
        april = resources.getString(R.string.screen_media_notes__date_header__april__title),
        march = resources.getString(R.string.screen_media_notes__date_header__march__title),
        may = resources.getString(R.string.screen_media_notes__date_header__may__title),
        june = resources.getString(R.string.screen_media_notes__date_header__june__title),
        july = resources.getString(R.string.screen_media_notes__date_header__july__title),
        august = resources.getString(R.string.screen_media_notes__date_header__august__title),
        september = resources.getString(R.string.screen_media_notes__date_header__september__title),
        october = resources.getString(R.string.screen_media_notes__date_header__october__title),
        november = resources.getString(R.string.screen_media_notes__date_header__november__title),
        december = resources.getString(R.string.screen_media_notes__date_header__december__title)
    )

    val hoursDateFormat = LocalDateTime.Format {
        hour(); char(':'); minute()
    }

    val dayOfYear = LocalDateTime.Format {
        dayOfMonth(); char(' '); monthName(monthNames)
    }
    return MediaNoteItem.CreatedTimeStamp(
        hourAndMinute = toLocalDateTime(TimeZone.currentSystemDefault()).format(hoursDateFormat),
        dayAndMonth = toLocalDateTime(TimeZone.currentSystemDefault()).format(dayOfYear)
    )
}
