package com.aitranslator.app.ui.dictionary

import com.aitranslator.app.domain.dictionary.DictionaryDefinition
import com.aitranslator.app.domain.dictionary.DictionaryEntry
import com.aitranslator.app.domain.dictionary.DictionarySourceType
import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.util.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DictionaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var onlineRepository: FakeWiktionaryDictionaryRepository
    private lateinit var offlineRepository: FakeOfflineDictionaryRepository
    private lateinit var viewModel: DictionaryViewModel

    @Before
    fun setUp() {
        onlineRepository = FakeWiktionaryDictionaryRepository()
        offlineRepository = FakeOfflineDictionaryRepository()
        viewModel = DictionaryViewModel(onlineRepository, offlineRepository)
    }

    @Test
    fun `initial state defaults to Spanish and Online source, no query`() {
        val state = viewModel.uiState.value
        assertEquals("", state.query)
        assertEquals(Language.SPANISH, state.language)
        assertEquals(DictionarySourceType.ONLINE, state.selectedSource)
        assertNull(state.entry)
    }

    @Test
    fun `onSearchClick with Online source uses the online repository`() = runTest {
        onlineRepository.result = Result.success(DictionaryEntry(word = "perro", definitions = listOf(DictionaryDefinition("Mamífero."))))
        viewModel.onQueryChanged("perro")

        viewModel.onSearchClick()
        advanceUntilIdle()

        assertEquals("perro", onlineRepository.lastWord)
        assertNull(offlineRepository.lastWord)
        assertEquals("perro", viewModel.uiState.value.entry?.word)
    }

    @Test
    fun `onSearchClick with Offline source uses the offline repository`() = runTest {
        offlineRepository.result = Result.success(DictionaryEntry(word = "perro", definitions = listOf(DictionaryDefinition("Mamífero."))))
        viewModel.onQueryChanged("perro")
        viewModel.onSourceSelected(DictionarySourceType.OFFLINE)

        viewModel.onSearchClick()
        advanceUntilIdle()

        assertEquals("perro", offlineRepository.lastWord)
        assertNull(onlineRepository.lastWord)
        assertEquals("perro", viewModel.uiState.value.entry?.word)
    }

    @Test
    fun `onSearchClick with blank query does nothing`() = runTest {
        viewModel.onSearchClick()
        advanceUntilIdle()

        assertNull(onlineRepository.lastWord)
        assertNull(offlineRepository.lastWord)
    }

    @Test
    fun `onSearchClick surfaces an offline lookup failure`() = runTest {
        offlineRepository.result = Result.failure(
            com.aitranslator.app.data.dictionary.DictionaryLookupException(
                "This word was not found",
                com.aitranslator.app.domain.dictionary.DictionaryErrorType.WORD_NOT_FOUND
            )
        )
        viewModel.onQueryChanged("asdkjasd")
        viewModel.onSourceSelected(DictionarySourceType.OFFLINE)

        viewModel.onSearchClick()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("This word was not found", state.errorMessage)
        assertNull(state.entry)
    }

    @Test
    fun `changing the query clears a previous result and error`() = runTest {
        onlineRepository.result = Result.success(DictionaryEntry(word = "perro", definitions = listOf(DictionaryDefinition("Mamífero."))))
        viewModel.onQueryChanged("perro")
        viewModel.onSearchClick()
        advanceUntilIdle()

        viewModel.onQueryChanged("perro grande")

        val state = viewModel.uiState.value
        assertNull(state.entry)
        assertNull(state.errorMessage)
    }

    @Test
    fun `onSourceSelected updates the selected source and clears results`() = runTest {
        onlineRepository.result = Result.success(DictionaryEntry(word = "perro", definitions = listOf(DictionaryDefinition("Mamífero."))))
        viewModel.onQueryChanged("perro")
        viewModel.onSearchClick()
        advanceUntilIdle()

        viewModel.onSourceSelected(DictionarySourceType.OFFLINE)

        val state = viewModel.uiState.value
        assertEquals(DictionarySourceType.OFFLINE, state.selectedSource)
        assertNull(state.entry)
    }

    @Test
    fun `onLanguageSelected updates the language and clears results`() = runTest {
        onlineRepository.result = Result.success(DictionaryEntry(word = "perro", definitions = listOf(DictionaryDefinition("Mamífero."))))
        viewModel.onQueryChanged("perro")
        viewModel.onSearchClick()
        advanceUntilIdle()

        viewModel.onLanguageSelected(Language.FRENCH)

        assertEquals(Language.FRENCH, viewModel.uiState.value.language)
        assertNull(viewModel.uiState.value.entry)
    }
}