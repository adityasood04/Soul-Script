package com.example.soulscript.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soulscript.data.Note
import com.example.soulscript.data.NoteRepository
import com.example.soulscript.screens.Mood
import com.example.soulscript.screens.moodOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiaryEntryUiState(
    val title: String = "",
    val content: String = "",
    val mood: Mood = moodOptions.first(),
    val sketchPath: String? = null
)

@HiltViewModel
class DiaryEntryViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryEntryUiState())
    val uiState: StateFlow<DiaryEntryUiState> = _uiState.asStateFlow()

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onContentChange(newContent: String) {
        _uiState.update { it.copy(content = newContent) }
    }

    fun onMoodChange(newMood: Mood) {
        _uiState.update { it.copy(mood = newMood) }
    }

    fun onSketchPathChange(newPath: String?) {
        _uiState.update { it.copy(sketchPath = newPath) }
    }

    fun saveEntry() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val newNote = Note(
                title = currentState.title,
                content = currentState.content,
                date = System.currentTimeMillis(),
                mood = currentState.mood.label,
                sketchPath = currentState.sketchPath
            )
            repository.insertNote(newNote)
        }
    }
}