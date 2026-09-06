package com.aitranslator.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aitranslator.app.domain.settings.AppLanguage
import com.aitranslator.app.domain.settings.AppTheme
import com.aitranslator.app.domain.settings.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val languageKey = stringPreferencesKey("app_language")
    private val themeKey = stringPreferencesKey("app_theme")

    override fun observeLanguage() = context.settingsDataStore.data.map { prefs ->
        prefs[languageKey]?.let { name ->
            runCatching { AppLanguage.valueOf(name) }.getOrNull()
        } ?: AppLanguage.SYSTEM_DEFAULT
    }

    override fun observeTheme() = context.settingsDataStore.data.map { prefs ->
        prefs[themeKey]?.let { name ->
            runCatching { AppTheme.valueOf(name) }.getOrNull()
        } ?: AppTheme.SYSTEM_DEFAULT
    }

    override suspend fun setLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { it[languageKey] = language.name }
    }

    override suspend fun setTheme(theme: AppTheme) {
        context.settingsDataStore.edit { it[themeKey] = theme.name }
    }
}