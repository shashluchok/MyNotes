package com.shashluchok.medianotes.domain.images

import android.content.Context
import android.provider.MediaStore
import com.shashluchok.medianotes.presentation.MediaImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

internal class GetGalleryImagesInteractorImpl : GetGalleryImagesInteractor {

    override suspend fun invoke(context: Context): ImmutableList<MediaImage> {
        val list = mutableListOf<MediaImage>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
        val selection = null
        val selectionArgs = null
        val sortOrder = MediaStore.Images.Media.DATE_ADDED + SORT_ORDER_POSTFIX

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dataColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val data = cursor.getString(dataColumn)
                list += MediaImage(id.toString(), data, MediaImage.Type.GALLERY)
            }
        }
        return list.toPersistentList()
    }

    companion object {
        private const val SORT_ORDER_POSTFIX = " DESC"
    }
}
