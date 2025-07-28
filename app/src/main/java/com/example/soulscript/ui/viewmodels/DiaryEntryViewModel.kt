package com.example.soulscript.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soulscript.data.Note
import com.example.soulscript.data.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryEntryViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    fun saveEntry(title: String, content: String, mood: String) {
        viewModelScope.launch {
            if (title.isNotBlank() && content.isNotBlank()) {
                val newNote = Note(
                    title = title,
                    content = content,
                    date = System.currentTimeMillis(),
                    mood = mood,
                    isImportant = false,
                    imageUri = null
                )
                repository.insertNote(newNote)
            }
        }
    }
}