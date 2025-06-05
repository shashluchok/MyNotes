package com.shashluchok.medianotes.domain.images

import android.content.Context
import com.shashluchok.medianotes.presentation.MediaImage
import kotlinx.collections.immutable.ImmutableList

internal fun interface GetGalleryImagesInteractor {
    suspend operator fun invoke(context: Context): ImmutableList<MediaImage>
}
