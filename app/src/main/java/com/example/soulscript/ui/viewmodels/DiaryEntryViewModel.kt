package com.example.soulscript.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soulscript.data.Note
import com.example.soulscript.data.NoteRepository
import com.example.soulscript.ui.screens.Mood
import com.example.soulscript.ui.screens.moodOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiaryEntryUiState(
    val noteId: Int? = null,
    val title: String = "",
    val content: String = "",
    val mood: Mood = moodOptions.first(),
    val sketchPath: String? = null,
    val audioPath:String?=null,
    val isNoteLoaded: Boolean = false
)

@HiltViewModel
class DiaryEntryViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Int? = if (savedStateHandle.get<Int>("noteId") == -1) null else savedStateHandle["noteId"]

    private val _uiState = MutableStateFlow(DiaryEntryUiState())
    val uiState: StateFlow<DiaryEntryUiState> = _uiState.asStateFlow()

    init {
        if (noteId != null && noteId != -1) {
            viewModelScope.launch {
                repository.getNoteById(noteId).firstOrNull()?.let { note ->
                    _uiState.update {
                        it.copy(
                            noteId = note.id,
                            title = note.title,
                            content = note.content,
                            mood = moodOptions.find { mood -> mood.label == note.mood } ?: moodOptions.first(),
                            sketchPath = note.sketchPath,
                            audioPath = note.audioPath,
                            isNoteLoaded = true
                        )
                    }
                }
            }
        } else {
            _uiState.update { it.copy(isNoteLoaded = true) }
        }

        savedStateHandle.getStateFlow<String?>("sketchPath", null)
            .onEach { path ->
                if (path != null) {
                    onSketchPathChange(path)
                    savedStateHandle.remove<String>("sketchPath")
                }
            }.launchIn(viewModelScope)
    }


    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
        savedStateHandle["currentTitle"] = newTitle
    }

    fun onContentChange(newContent: String) {
        _uiState.update { it.copy(content = newContent) }
        savedStateHandle["currentContent"] = newContent
    }

    fun onMoodChange(newMood: Mood) {
        _uiState.update { it.copy(mood = newMood) }
    }

    fun onSketchPathChange(newPath: String?) {
        _uiState.update { it.copy(sketchPath = newPath) }
    }
    fun onAudioPathChange(newPath: String?) {
        _uiState.update { it.copy(audioPath = newPath) }
    }
    fun onParametersReceived(title: String?, content: String?) {
        if (noteId == null) {
            _uiState.update {
                it.copy(
                    title = it.title.ifBlank { title ?: "" },
                    content = it.content.ifBlank { content ?: "" }
                )
            }
        }
    }

    fun saveEntry() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val noteToSave = Note(
                id = currentState.noteId ?: 0,
                title = currentState.title,
                content = currentState.content,
                date = System.currentTimeMillis(),
                mood = currentState.mood.label,
                sketchPath = currentState.sketchPath,
                audioPath = currentState.audioPath
            )

            if (currentState.noteId == null || currentState.noteId == -1) {
                repository.insertNote(noteToSave)
            } else {
                repository.updateNote(noteToSave)
            }
            savedStateHandle["lastMoodSaved"] = currentState.mood.label
        }
    }
}
