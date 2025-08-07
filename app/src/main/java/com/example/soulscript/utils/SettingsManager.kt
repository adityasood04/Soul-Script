package com.example.soulscript.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val SETTINGS_PREFERENCES_NAME = "settings_preferences"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_PREFERENCES_NAME)

enum class ThemeOption {
    Light, Dark, System
}

@Singleton
class SettingsManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val themeKey = stringPreferencesKey("theme_option")
    private val notificationsKey = booleanPreferencesKey("enable_notifications")
    private val userNameKey = stringPreferencesKey("user_name")
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")

    private val reminderHourKey = intPreferencesKey("reminder_hour")
    private val reminderMinuteKey = intPreferencesKey("reminder_minute")

    val themeFlow: Flow<ThemeOption> = context.dataStore.data.map { preferences ->
        ThemeOption.valueOf(preferences[themeKey] ?: ThemeOption.System.name)
    }

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[notificationsKey] ?: false
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[userNameKey] ?: ""
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[onboardingCompletedKey] ?: false
    }

    suspend fun setTheme(theme: ThemeOption) {
        context.dataStore.edit { settings ->
            settings[themeKey] = theme.name
        }
    }

    suspend fun setNotificationsEnabled(isEnabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[notificationsKey] = isEnabled
        }
    }

    suspend fun saveOnboardingState(name: String) {
        context.dataStore.edit { settings ->
            settings[userNameKey] = name
            settings[onboardingCompletedKey] = true
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { settings ->
            settings[userNameKey] = name
        }
    }

    val reminderTimeFlow: Flow<Pair<Int, Int>> = context.dataStore.data.map { preferences ->
        val hour = preferences[reminderHourKey] ?: 20 // Default to 8 PM (20:00)
        val minute = preferences[reminderMinuteKey] ?: 0
        Pair(hour, minute)
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { settings ->
            settings[reminderHourKey] = hour
            settings[reminderMinuteKey] = minute
        }
    }
}
