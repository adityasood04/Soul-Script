package com.example.soulscript.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soulscript.data.NoteRepository
import com.example.soulscript.utils.InsightGenerator
import com.example.soulscript.utils.MonthlyInsight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MonthlyRewindViewModel @Inject constructor(
    repository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val yearMonth: String = checkNotNull(savedStateHandle["yearMonth"])

    val uiState: StateFlow<MonthlyInsight> =
        repository.getNotesForMonth(yearMonth)
            .map { notes ->
                val monthName = try {
                    val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(yearMonth)
                    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date!!)
                } catch (e: Exception) { "Monthly Summary" }

                InsightGenerator.generateInsights(notes, monthName)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = MonthlyInsight("Loading...", null, emptyMap(), "Loading insights...", 0)
            )
}
