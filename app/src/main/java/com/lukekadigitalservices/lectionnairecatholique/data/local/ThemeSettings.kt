package com.lukekadigitalservices.lectionnairecatholique.data.local

import android.content.Context
import android.widget.Toast
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lukekadigitalservices.lectionnairecatholique.ui.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class ThemeSettings(private val context: Context) {

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val modeName = prefs[THEME_KEY] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(modeName)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = mode.name
        }
    }

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_mode")
    }
}