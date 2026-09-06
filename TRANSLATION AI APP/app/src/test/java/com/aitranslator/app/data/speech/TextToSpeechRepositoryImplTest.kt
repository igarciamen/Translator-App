package com.aitranslator.app.data.speech

import com.aitranslator.app.domain.speech.TextToSpeechEngine
import com.aitranslator.app.domain.translation.Language
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeTextToSpeechEngine : TextToSpeechEngine {
    var lastText: String? = null
    var lastLanguage: Language? = null
    var result: Result<Unit> = Result.success(Unit)
    var stopCallCount = 0

    override suspend fun speak(text: String, language: Language): Result<Unit> {
        lastText = text
        lastLanguage = language
        return result
    }

    override fun stop() {
        stopCallCount++
    }
}

class TextToSpeechRepositoryImplTest {

    @Test
    fun `blank text succeeds without reaching the engine`() = runTest {
        val engine = FakeTextToSpeechEngine()
        val repository = TextToSpeechRepositoryImpl(engine)

        val result = repository.speak("   ", Language.ENGLISH)

        assertTrue(result.isSuccess)
        assertEquals(null, engine.lastText)
    }

    @Test
    fun `non-blank text is forwarded to the engine`() = runTest {
        val engine = FakeTextToSpeechEngine()
        val repository = TextToSpeechRepositoryImpl(engine)

        repository.speak("Hello world", Language.ENGLISH)

        assertEquals("Hello world", engine.lastText)
        assertEquals(Language.ENGLISH, engine.lastLanguage)
    }

    @Test
    fun `engine failure is propagated`() = runTest {
        val engine = FakeTextToSpeechEngine()
        engine.result = Result.failure(TextToSpeechException("boom"))
        val repository = TextToSpeechRepositoryImpl(engine)

        val result = repository.speak("Hello", Language.ENGLISH)

        assertTrue(result.isFailure)
    }

    @Test
    fun `stop delegates to the engine`() {
        val engine = FakeTextToSpeechEngine()
        val repository = TextToSpeechRepositoryImpl(engine)

        repository.stop()

        assertEquals(1, engine.stopCallCount)
    }
}