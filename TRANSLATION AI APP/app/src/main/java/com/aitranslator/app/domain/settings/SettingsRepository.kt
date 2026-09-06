package com.aitranslator.app.domain.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeLanguage(): Flow<AppLanguage>
    fun observeTheme(): Flow<AppTheme>
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setTheme(theme: AppTheme)
}