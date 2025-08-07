package com.example.soulscript.ui.viewmodels

import com.example.soulscript.utils.PdfExporter

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soulscript.data.NoteRepository
import com.example.soulscript.utils.SettingsManager
import com.example.soulscript.utils.ThemeOption
import com.example.soulscript.utils.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ExportState {
    object Idle : ExportState()
    data class InProgress(val progress: Float) : ExportState()
    object Success : ExportState()
    object Error : ExportState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val noteRepository: NoteRepository
) : ViewModel() {

    val theme: StateFlow<ThemeOption> = settingsManager.themeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeOption.System
    )

    val notificationsEnabled: StateFlow<Boolean> = settingsManager.notificationsEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val onboardingCompletedFlow: Flow<Boolean> = settingsManager.onboardingCompletedFlow

    val userName: StateFlow<String> = settingsManager.userNameFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    fun setTheme(themeOption: ThemeOption) {
        viewModelScope.launch {
            settingsManager.setTheme(themeOption)
        }
    }

    fun setNotificationsEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setNotificationsEnabled(isEnabled)
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            settingsManager.setUserName(name)
        }
    }

    fun exportJournal(context: Context) {
        viewModelScope.launch {
            _exportState.value = ExportState.InProgress(0f)
            val notes = noteRepository.getAllNotes().first()
            val success = PdfExporter.exportToPdf(context, notes) { progress ->
                _exportState.value = ExportState.InProgress(progress)
            }
            _exportState.value = if (success) ExportState.Success else ExportState.Error
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }

    fun clearAllData() {
        viewModelScope.launch {
            noteRepository.deleteAllNotes()
        }
    }

    val reminderTime: StateFlow<Pair<Int, Int>> = settingsManager.reminderTimeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 20 to 0
    )

    fun setReminder(context: Context, isEnabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsManager.setNotificationsEnabled(isEnabled)
            settingsManager.setReminderTime(hour, minute)
            if (isEnabled) {
                AlarmScheduler.scheduleReminder(context, hour, minute)
            } else {
                AlarmScheduler.cancelReminder(context)
            }
        }
    }
}
