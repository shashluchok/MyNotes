package com.shashluchok.medianotes.sample.domain.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shashluchok.medianotes.sample.data.db.medianote.DbMediaNote
import com.shashluchok.medianotes.sample.domain.converters.InstantConverter
import com.shashluchok.medianotes.sample.domain.db.dao.MediaNotesDao

@Database(
    entities = [
        DbMediaNote::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(InstantConverter::class)
abstract class MediaNotesDatabase : RoomDatabase() {
    abstract fun mediaNotesDao(): MediaNotesDao
}
