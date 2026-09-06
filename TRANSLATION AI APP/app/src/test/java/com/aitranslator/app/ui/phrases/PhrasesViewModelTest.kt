package com.aitranslator.app.ui.phrases

import com.aitranslator.app.domain.phrases.PhraseCatalog
import com.aitranslator.app.domain.phrases.PhraseCategory
import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.ui.translate.FakeTextToSpeechRepository
import com.aitranslator.app.ui.translate.FakeTranslationRepository
import com.aitranslator.app.util.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PhrasesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var translationRepository: FakeTranslationRepository
    private lateinit var textToSpeechRepository: FakeTextToSpeechRepository
    private lateinit var customPhraseRepository: FakeCustomPhraseRepository
    private lateinit var viewModel: PhrasesViewModel

    @Before
    fun setUp() {
        translationRepository = FakeTranslationRepository()
        textToSpeechRepository = FakeTextToSpeechRepository()
        customPhraseRepository = FakeCustomPhraseRepository()
        translationRepository.result = Result.success("Traducción")
        viewModel = PhrasesViewModel(translationRepository, textToSpeechRepository, customPhraseRepository)
    }

    @Test
    fun `initial state defaults to Greetings category and Spanish target`() {
        val state = viewModel.uiState.value
        assertEquals(PhraseCategory.GREETINGS, state.selectedCategory)
        assertEquals(Language.SPANISH, state.targetLanguage)
    }

    @Test
    fun `on init all built-in greeting phrases appear and get translated`() = runTest {
        advanceUntilIdle()

        val expectedIds = PhraseCatalog.byCategory(PhraseCategory.GREETINGS).map { it.id }
        val state = viewModel.uiState.value
        expectedIds.forEach { id ->
            assertTrue(state.translations[id] is PhraseTranslationState.Success)
        }
    }

    @Test
    fun `onCategorySelected switches category and translates its phrases`() = runTest {
        advanceUntilIdle()

        viewModel.onCategorySelected(PhraseCategory.RESTAURANT)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(PhraseCategory.RESTAURANT, state.selectedCategory)
        val expectedIds = PhraseCatalog.byCategory(PhraseCategory.RESTAURANT).map { it.id }
        expectedIds.forEach { id ->
            assertTrue(state.translations[id] is PhraseTranslationState.Success)
        }
    }

    @Test
    fun `onAddPhraseClick adds a custom phrase to the current category and translates it`() = runTest {
        advanceUntilIdle()

        viewModel.onNewPhraseTextChanged("Where is the pharmacy?")
        viewModel.onAddPhraseClick()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val added = state.displayPhrases.find { it.phrase.englishText == "Where is the pharmacy?" }
        assertTrue(added != null && added.isCustom)
        assertTrue(state.translations[added?.phrase?.id] is PhraseTranslationState.Success)
        assertEquals("", state.newPhraseText)
    }

    @Test
    fun `onAddPhraseClick with blank text does nothing`() = runTest {
        advanceUntilIdle()
        val countBefore = viewModel.uiState.value.displayPhrases.size

        viewModel.onNewPhraseTextChanged("   ")
        viewModel.onAddPhraseClick()
        advanceUntilIdle()

        assertEquals(countBefore, viewModel.uiState.value.displayPhrases.size)
    }

    @Test
    fun `onDeletePhraseClick removes a custom phrase`() = runTest {
        advanceUntilIdle()
        viewModel.onNewPhraseTextChanged("Custom phrase")
        viewModel.onAddPhraseClick()
        advanceUntilIdle()

        val added = viewModel.uiState.value.displayPhrases.find { it.phrase.englishText == "Custom phrase" }
        requireNotNull(added)

        viewModel.onDeletePhraseClick(added.phrase.id)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.displayPhrases.none { it.phrase.id == added.phrase.id })
    }

    @Test
    fun `custom phrases from one category do not appear in another`() = runTest {
        advanceUntilIdle()
        viewModel.onNewPhraseTextChanged("Greeting custom phrase")
        viewModel.onAddPhraseClick()
        advanceUntilIdle()

        viewModel.onCategorySelected(PhraseCategory.RESTAURANT)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.displayPhrases.none { it.phrase.englishText == "Greeting custom phrase" })
    }

    @Test
    fun `onSpeakClick speaks the translated text of that phrase`() = runTest {
        advanceUntilIdle()

        val phrase = PhraseCatalog.byCategory(PhraseCategory.GREETINGS).first()
        viewModel.onSpeakClick(phrase)
        advanceUntilIdle()

        assertEquals("Traducción", textToSpeechRepository.lastSpokenText)
        assertNull(viewModel.uiState.value.speakingPhraseId)
    }

    @Test
    fun `stopSpeaking stops the engine and clears the speaking flag`() {
        viewModel.stopSpeaking()

        assertEquals(1, textToSpeechRepository.stopCallCount)
        assertNull(viewModel.uiState.value.speakingPhraseId)
    }
}