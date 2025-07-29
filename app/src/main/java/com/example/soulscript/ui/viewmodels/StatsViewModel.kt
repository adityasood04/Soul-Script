package com.example.soulscript.ui.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soulscript.data.Note
import com.example.soulscript.data.NoteRepository
import com.example.soulscript.screens.moodOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

data class MoodStat(val mood: String, val count: Int, val color: Color, val percentage: Float)

data class StatsUiState(
    val streak: Int = 0,
    val mostFrequentMood: String? = null,
    val heatmapData: Map<Long, Int> = emptyMap(),
    val totalEntries: Int = 0,
    val moodDistribution: List<MoodStat> = emptyList(),
    val monthlyEntries: List<Pair<String, Int>> = emptyList()
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    repository: NoteRepository
) : ViewModel() {

    val statsUiState: StateFlow<StatsUiState> =
        repository.getAllNotes().map { notes ->
            val streak = calculateStreak(notes)
            val mostFrequentMood = calculateMostFrequentMood(notes)
            val heatmapData = generateHeatmapData(notes)
            val moodDistribution = calculateMoodDistribution(notes)
            val monthlyEntries = calculateMonthlyEntries(notes)
            StatsUiState(
                streak = streak,
                mostFrequentMood = mostFrequentMood,
                heatmapData = heatmapData,
                totalEntries = notes.size,
                moodDistribution = moodDistribution,
                monthlyEntries = monthlyEntries
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = StatsUiState()
        )

    private fun calculateStreak(notes: List<Note>): Int {
        if (notes.isEmpty()) return 0

        val distinctDays = notes
            .map { getDayFromTimestamp(it.date) }
            .distinct()
            .sortedDescending()

        var currentStreak = 0
        val today = Calendar.getInstance()
        val todayTimestamp = getDayFromTimestamp(today.timeInMillis)

        if (distinctDays.first() == todayTimestamp || distinctDays.first() == todayTimestamp - 1) {
            currentStreak = 1
            for (i in 0 until distinctDays.size - 1) {
                if (distinctDays[i] - distinctDays[i+1] == 1L) {
                    currentStreak++
                } else {
                    break
                }
            }
        }
        if (distinctDays.first() == todayTimestamp - 1 && currentStreak == 0 && distinctDays.size == 1) {
            currentStreak = 1
        }

        return currentStreak
    }

    private fun calculateMostFrequentMood(notes: List<Note>): String? {
        if (notes.isEmpty()) return null
        return notes.groupingBy { it.mood }.eachCount().maxByOrNull { it.value }?.key
    }

    private fun generateHeatmapData(notes: List<Note>): Map<Long, Int> {
        return notes.groupBy { getDayFromTimestamp(it.date) }
            .mapValues { it.value.size }
    }

    private fun getDayFromTimestamp(timestamp: Long): Long {
        val localTimeZone = TimeZone.getDefault()
        val calendar = Calendar.getInstance(localTimeZone).apply {
            timeInMillis = timestamp
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis / (1000 * 60 * 60 * 24) + 1  // TODO recheck day logic
    }

    private fun calculateMoodDistribution(notes: List<Note>): List<MoodStat> {
        if (notes.isEmpty()) return emptyList()
        val totalNotes = notes.size.toFloat()
        return notes
            .groupingBy { it.mood }
            .eachCount()
            .map { (moodLabel, count) ->
                val moodObject = moodOptions.find { it.label == moodLabel }
                val color = moodObject?.gradient?.first() ?: Color.Gray
                MoodStat(
                    mood = moodLabel,
                    count = count,
                    color = color,
                    percentage = (count / totalNotes) * 100
                )
            }
            .sortedByDescending { it.count }
    }

    private fun calculateMonthlyEntries(notes: List<Note>): List<Pair<String, Int>> {
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val sixMonthsAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -5); set(Calendar.DAY_OF_MONTH, 1) }

        val monthLabels = (0..5).map {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -it)
            monthFormat.format(cal.time)
        }.reversed()

        val monthlyCounts = notes
            .filter { it.date >= sixMonthsAgo.timeInMillis }
            .groupBy { monthFormat.format(Date(it.date)) }
            .mapValues { it.value.size }

        return monthLabels.map { it to (monthlyCounts[it] ?: 0) }
    }
}
