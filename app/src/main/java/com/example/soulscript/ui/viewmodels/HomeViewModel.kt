package com.example.soulscript.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soulscript.data.Note
import com.example.soulscript.data.NoteRepository
import com.example.soulscript.data.Quotes
import com.example.soulscript.data.WellnessTips
import com.example.soulscript.utils.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val userName: String = " ",
    val recentNotes: List<Note> = emptyList(),
    val onThisDayNote: Note? = null,
    val quoteOfTheDay: Pair<String, String> = "" to "",
    val wellnessTip: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val settingsManager: SettingsManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _wellnessTip = MutableStateFlow<String?>(null)
    val uiState: StateFlow<HomeUiState> =
        combine(
            settingsManager.userNameFlow,
            repository.getAllNotes(),
            _wellnessTip
        ) { userName, allNotes, tip ->
            val firstName = userName.split(" ").firstOrNull() ?: userName
            val recent = allNotes.take(5)

            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val quoteIndex = dayOfYear % Quotes.quoteList.size
            val dailyQuote = Quotes.quoteList[quoteIndex]

            HomeUiState(
                userName = firstName,
                recentNotes = recent,
                quoteOfTheDay = dailyQuote,
                wellnessTip = tip
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = HomeUiState()
        )

    val onThisDayNote: StateFlow<Note?> = flow {
        val today = Calendar.getInstance()
        val month = today.get(Calendar.MONTH) + 1
        val day = today.get(Calendar.DAY_OF_MONTH)
        val monthDayString = String.format("%02d-%02d", month, day)

        repository.getNotesOnThisDay(monthDayString)
            .map { notes ->
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

    fun onMoodSaved(mood: String) {
        _wellnessTip.value = when (mood) {
            "Sad" -> WellnessTips.sadTips.random()
            "Stressed" -> WellnessTips.stressedTips.random()
            "Tired" -> WellnessTips.tiredTips.random()
            else -> null
        }
    }

    fun dismissWellnessTip() {
        _wellnessTip.value = null
    }
}