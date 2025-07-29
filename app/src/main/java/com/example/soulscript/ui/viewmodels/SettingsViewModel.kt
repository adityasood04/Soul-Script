package com.example.soulscript.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soulscript.data.NoteRepository
import com.example.soulscript.data.SettingsManager
import com.example.soulscript.data.ThemeOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    fun clearAllData() {
        viewModelScope.launch {
            //TODO : Add logic in repository
        }
    }
}
