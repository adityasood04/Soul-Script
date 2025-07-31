package com.example.soulscript.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes_table ORDER BY date DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes_table WHERE id = :id")
    fun getNoteById(id: Int): Flow<Note>

    @Query("SELECT * FROM notes_table WHERE strftime('%m-%d', date / 1000, 'unixepoch') = :monthDay ORDER BY date DESC")
    fun getNotesOnThisDay(monthDay: String): Flow<List<Note>>

    @Query("DELETE FROM notes_table")
    suspend fun deleteAllNotes()
}
