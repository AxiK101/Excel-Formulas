package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppThemeMode
import com.example.data.model.ExcelDialect
import com.example.data.model.ExcelPlatform
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "formulagrid_prefs")

data class UserPreferences(
    val preferredDialect: ExcelDialect = ExcelDialect.ENGLISH,
    val preferredPlatform: ExcelPlatform = ExcelPlatform.EXCEL_365,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val DIALECT = stringPreferencesKey("preferred_dialect")
        val PLATFORM = stringPreferencesKey("preferred_platform")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val dialect = ExcelDialect.entries.firstOrNull {
            it.name == prefs[Keys.DIALECT]
        } ?: ExcelDialect.ENGLISH
        val platform = ExcelPlatform.entries.firstOrNull {
            it.name == prefs[Keys.PLATFORM]
        } ?: ExcelPlatform.EXCEL_365
        val themeMode = AppThemeMode.entries.firstOrNull {
            it.name == prefs[Keys.THEME_MODE]
        } ?: AppThemeMode.SYSTEM
        UserPreferences(
            preferredDialect = dialect,
            preferredPlatform = platform,
            themeMode = themeMode
        )
    }

    suspend fun setDialect(dialect: ExcelDialect) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DIALECT] = dialect.name
        }
    }

    suspend fun setPlatform(platform: ExcelPlatform) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PLATFORM] = platform.name
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }
}
