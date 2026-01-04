package id.antasari.cityreport.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Theme Manager to handle dark mode state
 */
object ThemeManager {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    
    private var _isDarkMode by mutableStateOf(false)
    val isDarkMode: Boolean get() = _isDarkMode
    
    fun toggleDarkMode() {
        _isDarkMode = !_isDarkMode
    }
    
    fun updateDarkMode(enabled: Boolean) {
        _isDarkMode = enabled
    }
    
    // Get dark mode preference flow
    fun getDarkModeFlow(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }
    }
    
    // Save dark mode preference
    suspend fun saveDarkMode(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
        _isDarkMode = enabled
    }
}
