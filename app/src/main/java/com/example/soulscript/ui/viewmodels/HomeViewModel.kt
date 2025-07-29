package com.example.soulscript.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soulscript.data.Note
import com.example.soulscript.data.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val recentNotes: List<Note> = emptyList(),
    val onThisDayNote: Note? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        repository.getAllNotes().map { allNotes ->
            val recent = allNotes.take(5) // Get the 5 most recent notes
            HomeUiState(recentNotes = recent)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = HomeUiState()
        )

    val onThisDayNote: StateFlow<Note?> = flow {
        val today = Calendar.getInstance()
        val month = today.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        val day = today.get(Calendar.DAY_OF_MONTH)
        val monthDayString = String.format("%02d-%02d", month, day)

        repository.getNotesOnThisDay(monthDayString)
            .map { notes ->
                // Find a note from a previous year, not today's date
                notes.firstOrNull {
                    val noteCalendar = Calendar.getInstance().apply { timeInMillis = it.date }
                    noteCalendar.get(Calendar.YEAR) < today.get(Calendar.YEAR)
                }
            }
            .collect { emit(it) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null
    )
}