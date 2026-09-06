package com.aitranslator.app.ui.settings

import com.aitranslator.app.domain.settings.AppLanguage
import com.aitranslator.app.domain.settings.AppTheme

data class SettingsUiState(
    val language: AppLanguage = AppLanguage.SYSTEM_DEFAULT,
    val theme: AppTheme = AppTheme.SYSTEM_DEFAULT
)