package com.example.soulscript.di

import android.content.Context
import com.example.soulscript.data.NoteDao
import com.example.soulscript.data.NoteDatabase
import com.example.soulscript.data.NoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideNoteDatabase(@ApplicationContext context: Context): NoteDatabase {
        return NoteDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun provideNoteDao(database: NoteDatabase): NoteDao {
        return database.noteDao()
    }

    @Singleton
    @Provides
    fun provideNoteRepository(noteDao: NoteDao): NoteRepository {
        return NoteRepository(noteDao)
    }
}