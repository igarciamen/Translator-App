package com.aitranslator.app.ui.translate

import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.util.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TranslateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeTranslationRepository
    private lateinit var detectionRepository: FakeLanguageDetectionRepository
    private lateinit var historyRepository: FakeHistoryRepository
    private lateinit var textToSpeechRepository: FakeTextToSpeechRepository
    private lateinit var speechToTextRepository: FakeSpeechToTextRepository
    private lateinit var viewModel: TranslateViewModel

    @Before
    fun setUp() {
        repository = FakeTranslationRepository()
        detectionRepository = FakeLanguageDetectionRepository()
        historyRepository = FakeHistoryRepository()
        textToSpeechRepository = FakeTextToSpeechRepository()
        speechToTextRepository = FakeSpeechToTextRepository()
        viewModel = TranslateViewModel(
            repository, detectionRepository, historyRepository,
            textToSpeechRepository, speechToTextRepository
        )
    }

    @Test
    fun `initial state defaults to Spanish to English`() {
        val state = viewModel.uiState.value
        assertEquals(Language.SPANISH, state.sourceLanguage)
        assertEquals(Language.ENGLISH, state.targetLanguage)
        assertEquals("", state.inputText)
        assertFalse(state.isTranslating)
        assertNull(state.errorMessage)
    }

    @Test
    fun `onInputChanged updates the input text`() {
        viewModel.onInputChanged("Hola")
        assertEquals("Hola", viewModel.uiState.value.inputText)
    }

    @Test
    fun `translate with blank input does not call the repository`() = runTest {
        viewModel.onInputChanged("   ")
        viewModel.translate()
        advanceUntilIdle()

        assertNull(repository.lastTranslatedText)
    }

    @Test
    fun `translate sends the currently selected languages to the repository`() = runTest {
        repository.result = Result.success("Bonjour")
        viewModel.onSourceLanguageSelected(Language.ENGLISH)
        viewModel.onTargetLanguageSelected(Language.FRENCH)
        viewModel.onInputChanged("Hello")

        viewModel.translate()
        advanceUntilIdle()

        assertEquals(Language.ENGLISH, repository.lastSourceLanguage)
        assertEquals(Language.FRENCH, repository.lastTargetLanguage)
        assertEquals("Bonjour", viewModel.uiState.value.translatedText)
    }

    @Test
    fun `translate failure updates state with an error message`() = runTest {
        repository.result = Result.failure(RuntimeException("Network unavailable"))
        viewModel.onInputChanged("Hola")

        viewModel.translate()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Network unavailable", state.errorMessage)
        assertFalse(state.isTranslating)
        assertTrue(state.translatedText.isEmpty())
    }

    @Test
    fun `swapLanguages exchanges source and target`() {
        viewModel.onSourceLanguageSelected(Language.GERMAN)
        viewModel.onTargetLanguageSelected(Language.ITALIAN)

        viewModel.swapLanguages()

        val state = viewModel.uiState.value
        assertEquals(Language.ITALIAN, state.sourceLanguage)
        assertEquals(Language.GERMAN, state.targetLanguage)
    }

    @Test
    fun `selecting a source language equal to the current target swaps them instead`() {
        viewModel.onSourceLanguageSelected(Language.ENGLISH)

        val state = viewModel.uiState.value
        assertEquals(Language.ENGLISH, state.sourceLanguage)
        assertEquals(Language.SPANISH, state.targetLanguage)
    }

    @Test
    fun `selecting a target language equal to the current source swaps them instead`() {
        viewModel.onTargetLanguageSelected(Language.SPANISH)

        val state = viewModel.uiState.value
        assertEquals(Language.ENGLISH, state.sourceLanguage)
        assertEquals(Language.SPANISH, state.targetLanguage)
    }

    @Test
    fun `initial state reflects already downloaded language models`() = runTest {
        val customRepository = FakeTranslationRepository()
        customRepository.downloadedLanguages.addAll(listOf(Language.SPANISH, Language.ENGLISH))
        val customViewModel = TranslateViewModel(
            customRepository, detectionRepository, historyRepository,
            textToSpeechRepository, speechToTextRepository
        )

        advanceUntilIdle()

        val state = customViewModel.uiState.value
        assertTrue(state.sourceLanguageDownloaded)
        assertTrue(state.targetLanguageDownloaded)
    }

    @Test
    fun `initial state shows models as not downloaded when none are cached`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.sourceLanguageDownloaded)
        assertFalse(state.targetLanguageDownloaded)
    }

    @Test
    fun `selecting a new target language refreshes its download status`() = runTest {
        repository.downloadedLanguages.add(Language.FRENCH)
        viewModel.onTargetLanguageSelected(Language.FRENCH)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.targetLanguageDownloaded)
    }

    @Test
    fun `downloadMissingModels downloads both languages when neither is cached`() = runTest {
        advanceUntilIdle()

        viewModel.downloadMissingModels()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isDownloadingModel)
        assertTrue(state.sourceLanguageDownloaded)
        assertTrue(state.targetLanguageDownloaded)
        assertTrue(Language.SPANISH in repository.downloadedLanguages)
        assertTrue(Language.ENGLISH in repository.downloadedLanguages)
    }

    @Test
    fun `downloadMissingModels respects the wifi-only setting`() = runTest {
        advanceUntilIdle()
        viewModel.onWifiOnlyToggled(false)

        viewModel.downloadMissingModels()
        advanceUntilIdle()

        assertEquals(false, repository.lastDownloadRequiredWifi)
    }

    @Test
    fun `downloadMissingModels does nothing when both languages are already downloaded`() = runTest {
        repository.downloadedLanguages.addAll(listOf(Language.SPANISH, Language.ENGLISH))
        val customViewModel = TranslateViewModel(
            repository, detectionRepository, historyRepository,
            textToSpeechRepository, speechToTextRepository
        )
        advanceUntilIdle()

        customViewModel.downloadMissingModels()
        advanceUntilIdle()

        assertEquals(null, repository.lastDownloadRequiredWifi)
    }

    @Test
    fun `downloadMissingModels surfaces a failure message`() = runTest {
        advanceUntilIdle()
        repository.downloadResult = Result.failure(RuntimeException("No network"))

        viewModel.downloadMissingModels()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isDownloadingModel)
        assertEquals("No network", state.downloadError)
    }

    @Test
    fun `onWifiOnlyToggled updates the state`() {
        viewModel.onWifiOnlyToggled(false)
        assertFalse(viewModel.uiState.value.downloadWifiOnly)
    }

    @Test
    fun `translate without auto-detect does not call the language detector`() = runTest {
        viewModel.onInputChanged("Hola")

        viewModel.translate()
        advanceUntilIdle()

        assertNull(detectionRepository.lastInput)
    }

    @Test
    fun `translate with auto-detect enabled detects language before translating`() = runTest {
        repository.result = Result.success("Bonjour")
        detectionRepository.result = Result.success(Language.FRENCH)
        viewModel.onAutoDetectToggled(true)
        viewModel.onInputChanged("Hello")

        viewModel.translate()
        advanceUntilIdle()

        assertEquals("Hello", detectionRepository.lastInput)
        assertEquals(Language.FRENCH, repository.lastSourceLanguage)
    }

    @Test
    fun `translate with auto-detect updates the source language in the state`() = runTest {
        detectionRepository.result = Result.success(Language.GERMAN)
        viewModel.onAutoDetectToggled(true)
        viewModel.onInputChanged("Guten Tag")

        viewModel.translate()
        advanceUntilIdle()

        assertEquals(Language.GERMAN, viewModel.uiState.value.sourceLanguage)
    }

    @Test
    fun `translate with auto-detect failure shows a detection error and does not translate`() = runTest {
        detectionRepository.result = Result.failure(RuntimeException("Could not determine the language of the text"))
        viewModel.onAutoDetectToggled(true)
        viewModel.onInputChanged("???")

        viewModel.translate()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Could not determine the language of the text", state.detectionError)
        assertNull(repository.lastTranslatedText)
        assertTrue(state.translatedText.isEmpty())
    }

    @Test
    fun `onAutoDetectToggled updates the state`() {
        viewModel.onAutoDetectToggled(true)
        assertTrue(viewModel.uiState.value.isAutoDetectEnabled)
    }

    @Test
    fun `enabling auto-detect clears a previous detection error`() = runTest {
        detectionRepository.result = Result.failure(RuntimeException("boom"))
        viewModel.onAutoDetectToggled(true)
        viewModel.onInputChanged("???")
        viewModel.translate()
        advanceUntilIdle()

        viewModel.onAutoDetectToggled(false)
        viewModel.onAutoDetectToggled(true)

        assertNull(viewModel.uiState.value.detectionError)
    }

    @Test
    fun `a successful translation is saved to history`() = runTest {
        repository.result = Result.success("Hello")
        viewModel.onInputChanged("Hola")

        viewModel.translate()
        advanceUntilIdle()

        assertEquals(1, historyRepository.savedCalls.size)
        val saved = historyRepository.savedCalls.first()
        assertEquals("Hola", saved.sourceText)
        assertEquals("Hello", saved.translatedText)
        assertEquals(Language.SPANISH, saved.sourceLanguage)
        assertEquals(Language.ENGLISH, saved.targetLanguage)
    }

    @Test
    fun `a failed translation is not saved to history`() = runTest {
        repository.result = Result.failure(RuntimeException("boom"))
        viewModel.onInputChanged("Hola")

        viewModel.translate()
        advanceUntilIdle()

        assertTrue(historyRepository.savedCalls.isEmpty())
    }

    @Test
    fun `a translation via auto-detect is saved with the detected language`() = runTest {
        repository.result = Result.success("Bonjour")
        detectionRepository.result = Result.success(Language.FRENCH)
        viewModel.onAutoDetectToggled(true)
        viewModel.onInputChanged("Hello")

        viewModel.translate()
        advanceUntilIdle()

        assertEquals(1, historyRepository.savedCalls.size)
        assertEquals(Language.FRENCH, historyRepository.savedCalls.first().sourceLanguage)
    }

    @Test
    fun `loadFromHistory populates the state with the given entry data`() = runTest {
        viewModel.loadFromHistory(
            sourceText = "Hola",
            translatedText = "Hello",
            sourceLanguage = Language.SPANISH,
            targetLanguage = Language.ENGLISH
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Hola", state.inputText)
        assertEquals("Hello", state.translatedText)
        assertEquals(Language.SPANISH, state.sourceLanguage)
        assertEquals(Language.ENGLISH, state.targetLanguage)
    }

    @Test
    fun `speakTranslation does nothing when there is no translated text`() = runTest {
        viewModel.speakTranslation()
        advanceUntilIdle()

        assertNull(textToSpeechRepository.lastSpokenText)
    }

    @Test
    fun `speakTranslation speaks the translated text in the target language`() = runTest {
        repository.result = Result.success("Hello")
        viewModel.onInputChanged("Hola")
        viewModel.translate()
        advanceUntilIdle()

        viewModel.speakTranslation()
        advanceUntilIdle()

        assertEquals("Hello", textToSpeechRepository.lastSpokenText)
        assertEquals(Language.ENGLISH, textToSpeechRepository.lastSpokenLanguage)
        assertFalse(viewModel.uiState.value.isSpeaking)
    }

    @Test
    fun `speakTranslation surfaces a speech failure`() = runTest {
        repository.result = Result.success("Hello")
        viewModel.onInputChanged("Hola")
        viewModel.translate()
        advanceUntilIdle()

        textToSpeechRepository.result = Result.failure(RuntimeException("No TTS engine available"))
        viewModel.speakTranslation()
        advanceUntilIdle()

        assertEquals("No TTS engine available", viewModel.uiState.value.speechError)
        assertFalse(viewModel.uiState.value.isSpeaking)
    }

    @Test
    fun `stopSpeaking stops the engine and clears the speaking flag`() = runTest {
        repository.result = Result.success("Hello")
        viewModel.onInputChanged("Hola")
        viewModel.translate()
        advanceUntilIdle()

        viewModel.stopSpeaking()

        assertEquals(1, textToSpeechRepository.stopCallCount)
        assertFalse(viewModel.uiState.value.isSpeaking)
    }

    @Test
    fun `onCleared stops any ongoing speech`() = runTest {
        val store = androidx.lifecycle.ViewModelStore()
        store.put("translateViewModel", viewModel)

        repository.result = Result.success("Hello")
        viewModel.onInputChanged("Hola")
        viewModel.translate()
        advanceUntilIdle()

        store.clear()

        assertEquals(1, textToSpeechRepository.stopCallCount)
    }

    @Test
    fun `speakTranslation is a no-op while already speaking`() = runTest {
        repository.result = Result.success("Hello")
        viewModel.onInputChanged("Hola")
        viewModel.translate()
        advanceUntilIdle()

        viewModel.speakTranslation()
        viewModel.speakTranslation()
        advanceUntilIdle()

        assertEquals("Hello", textToSpeechRepository.lastSpokenText)
    }

    @Test
    fun `startListening does nothing while already listening`() = runTest {
        speechToTextRepository.result = Result.success("Hola")
        viewModel.startListening()
        viewModel.startListening()
        advanceUntilIdle()

        assertEquals(Language.SPANISH, speechToTextRepository.lastLanguage)
    }

    @Test
    fun `startListening fills the input text with the recognized speech`() = runTest {
        speechToTextRepository.result = Result.success("Buenos días")

        viewModel.startListening()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Buenos días", state.inputText)
        assertFalse(state.isListening)
        assertNull(state.listeningError)
    }

    @Test
    fun `startListening uses the current source language`() = runTest {
        speechToTextRepository.result = Result.success("Bonjour")
        viewModel.onSourceLanguageSelected(Language.FRENCH)

        viewModel.startListening()
        advanceUntilIdle()

        assertEquals(Language.FRENCH, speechToTextRepository.lastLanguage)
    }

    @Test
    fun `startListening surfaces a recognition failure`() = runTest {
        speechToTextRepository.result = Result.failure(RuntimeException("Could not understand the audio"))

        viewModel.startListening()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Could not understand the audio", state.listeningError)
        assertFalse(state.isListening)
    }
}