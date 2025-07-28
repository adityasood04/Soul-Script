package com.example.soulscript.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository that provides insert, update, delete, and retrieve of [Note] from a given data source.
 */
class NoteRepository @Inject constructor(private val noteDao: NoteDao) {

    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    fun getNoteById(id: Int): Flow<Note> = noteDao.getNoteById(id)

    suspend fun insertNote(note: Note) {
        noteDao.insertNote(note)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }
}