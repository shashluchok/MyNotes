package com.shashluchok.medianotes.sample.domain.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shashluchok.medianotes.sample.data.db.medianote.DbMediaNote
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaNotesDao {

    @Query("SELECT * FROM mediaNotes")
    fun getMediaNotesFlow(): Flow<List<DbMediaNote>>

    @Query("SELECT * FROM mediaNotes WHERE id =:id ")
    fun getMediaNoteById(id: String): DbMediaNote?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateMediaNote(dbMediaNote: DbMediaNote)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMediaNote(dbMediaNote: DbMediaNote)

    @Query("DELETE FROM mediaNotes WHERE id IN (:noteIds)")
    fun deleteMediaNotes(noteIds: List<String>)
}
