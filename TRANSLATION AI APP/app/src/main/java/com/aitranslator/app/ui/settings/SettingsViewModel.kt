package com.aitranslator.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitranslator.app.domain.settings.AppLanguage
import com.aitranslator.app.domain.settings.AppTheme
import com.aitranslator.app.domain.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.observeLanguage(),
        repository.observeTheme()
    ) { language, theme ->
        SettingsUiState(language = language, theme = theme)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState()
    )

    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch {
            repository.setLanguage(language)
        }
    }

    fun onThemeSelected(theme: AppTheme) {
        viewModelScope.launch {
            repository.setTheme(theme)
        }
    }
}