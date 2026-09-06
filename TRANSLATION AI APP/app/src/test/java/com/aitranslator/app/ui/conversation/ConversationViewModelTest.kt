package com.aitranslator.app.ui.conversation

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

class ConversationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var speechToTextRepository: FakeSpeechToTextRepository
    private lateinit var translationRepository: FakeTranslationRepository
    private lateinit var textToSpeechRepository: FakeTextToSpeechRepository
    private lateinit var viewModel: ConversationViewModel

    @Before
    fun setUp() {
        speechToTextRepository = FakeSpeechToTextRepository()
        translationRepository = FakeTranslationRepository()
        textToSpeechRepository = FakeTextToSpeechRepository()
        viewModel = ConversationViewModel(speechToTextRepository, translationRepository, textToSpeechRepository)
    }

    @Test
    fun `initial state defaults to Spanish for speaker A and English for speaker B`() {
        val state = viewModel.uiState.value
        assertEquals(Language.SPANISH, state.languageA)
        assertEquals(Language.ENGLISH, state.languageB)
        assertNull(state.lastMessage)
    }

    @Test
    fun `onLanguageASelected updates speaker A`() {
        viewModel.onLanguageASelected(Language.FRENCH)
        assertEquals(Language.FRENCH, viewModel.uiState.value.languageA)
    }

    @Test
    fun `onLanguageBSelected updates speaker B`() {
        viewModel.onLanguageBSelected(Language.GERMAN)
        assertEquals(Language.GERMAN, viewModel.uiState.value.languageB)
    }

    @Test
    fun `selecting speaker A language equal to speaker B swaps them instead`() {
        viewModel.onLanguageASelected(Language.ENGLISH)

        val state = viewModel.uiState.value
        assertEquals(Language.ENGLISH, state.languageA)
        assertEquals(Language.SPANISH, state.languageB)
    }

    @Test
    fun `selecting speaker B language equal to speaker A swaps them instead`() {
        viewModel.onLanguageBSelected(Language.SPANISH)

        val state = viewModel.uiState.value
        assertEquals(Language.ENGLISH, state.languageA)
        assertEquals(Language.SPANISH, state.languageB)
    }

    @Test
    fun `onSwapSpeakers exchanges both languages`() {
        viewModel.onLanguageASelected(Language.ITALIAN)
        viewModel.onLanguageBSelected(Language.GERMAN)

        viewModel.onSwapSpeakers()

        val state = viewModel.uiState.value
        assertEquals(Language.GERMAN, state.languageA)
        assertEquals(Language.ITALIAN, state.languageB)
    }

    @Test
    fun `onMicATapped recognizes speech in language A and translates to language B`() = runTest {
        speechToTextRepository.result = Result.success("Hola")
        translationRepository.result = Result.success("Hello")

        viewModel.onMicATapped()
        advanceUntilIdle()

        assertEquals(Language.SPANISH, speechToTextRepository.lastLanguage)
        val message = viewModel.uiState.value.lastMessage
        assertEquals(true, message?.spokenBySideA)
        assertEquals("Hello", message?.translatedText)
    }

    @Test
    fun `onMicBTapped recognizes speech in language B and translates to language A`() = runTest {
        speechToTextRepository.result = Result.success("Hello")
        translationRepository.result = Result.success("Hola")

        viewModel.onMicBTapped()
        advanceUntilIdle()

        assertEquals(Language.ENGLISH, speechToTextRepository.lastLanguage)
        assertEquals(false, viewModel.uiState.value.lastMessage?.spokenBySideA)
    }

    @Test
    fun `onMicATapped surfaces a recognition failure on side A only`() = runTest {
        speechToTextRepository.result = Result.failure(RuntimeException("Could not understand the audio"))

        viewModel.onMicATapped()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Could not understand the audio", state.errorA)
        assertNull(state.errorB)
    }

    @Test
    fun `onMicATapped does nothing while side B is already listening`() = runTest {
        speechToTextRepository.result = Result.success("Hello")
        viewModel.onMicBTapped()

        viewModel.onMicATapped()
        advanceUntilIdle()

        assertEquals(Language.ENGLISH, speechToTextRepository.lastLanguage)
        assertFalse(viewModel.uiState.value.isListeningA)
    }

    @Test
    fun `a successful translation from A is spoken aloud on side B in language B`() = runTest {
        speechToTextRepository.result = Result.success("Hola")
        translationRepository.result = Result.success("Hello")

        viewModel.onMicATapped()
        advanceUntilIdle()

        assertEquals("Hello", textToSpeechRepository.lastSpokenText)
        assertEquals(Language.ENGLISH, textToSpeechRepository.lastSpokenLanguage)
    }

    @Test
    fun `stopSpeaking stops the engine and clears both speaking flags`() {
        viewModel.stopSpeaking()

        assertEquals(1, textToSpeechRepository.stopCallCount)
        val state = viewModel.uiState.value
        assertFalse(state.isSpeakingA)
        assertFalse(state.isSpeakingB)
    }
}