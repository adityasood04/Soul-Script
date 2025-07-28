package com.example.soulscript.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soulscript.data.Note
import com.example.soulscript.data.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    repository: NoteRepository
) : ViewModel() {

    /**
     * Holds history screen UI state.
     * The data is retrieved from the repository and mapped to the UI state.
     */
    val historyUiState: StateFlow<HistoryUiState> =
        repository.getAllNotes().map { HistoryUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = HistoryUiState()
            )
}

/**
 * Represents the UI state for the History screen.
 */
data class HistoryUiState(val noteList: List<Note> = listOf())
