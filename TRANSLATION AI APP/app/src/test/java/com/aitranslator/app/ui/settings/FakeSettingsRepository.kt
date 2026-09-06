package com.aitranslator.app.ui.settings

import com.aitranslator.app.domain.settings.AppLanguage
import com.aitranslator.app.domain.settings.AppTheme
import com.aitranslator.app.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository : SettingsRepository {
    val language = MutableStateFlow(AppLanguage.SYSTEM_DEFAULT)
    val theme = MutableStateFlow(AppTheme.SYSTEM_DEFAULT)

    override fun observeLanguage() = language
    override fun observeTheme() = theme

    override suspend fun setLanguage(language: AppLanguage) {
        this.language.value = language
    }

    override suspend fun setTheme(theme: AppTheme) {
        this.theme.value = theme
    }
}