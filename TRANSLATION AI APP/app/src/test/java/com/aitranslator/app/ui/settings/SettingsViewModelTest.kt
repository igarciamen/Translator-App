package com.aitranslator.app.ui.settings

import com.aitranslator.app.domain.settings.AppLanguage
import com.aitranslator.app.domain.settings.AppTheme
import com.aitranslator.app.util.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeSettingsRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        repository = FakeSettingsRepository()
        viewModel = SettingsViewModel(repository)
    }

    @Test
    fun `initial state defaults to system language and theme`() {
        val state = viewModel.uiState.value
        assertEquals(AppLanguage.SYSTEM_DEFAULT, state.language)
        assertEquals(AppTheme.SYSTEM_DEFAULT, state.theme)
    }

    @Test
    fun `onLanguageSelected persists the new language`() = runTest {
        viewModel.onLanguageSelected(AppLanguage.SPANISH)
        advanceUntilIdle()

        assertEquals(AppLanguage.SPANISH, repository.language.value)
        assertEquals(AppLanguage.SPANISH, viewModel.uiState.value.language)
    }

    @Test
    fun `onThemeSelected persists the new theme`() = runTest {
        viewModel.onThemeSelected(AppTheme.DARK)
        advanceUntilIdle()

        assertEquals(AppTheme.DARK, repository.theme.value)
        assertEquals(AppTheme.DARK, viewModel.uiState.value.theme)
    }
}