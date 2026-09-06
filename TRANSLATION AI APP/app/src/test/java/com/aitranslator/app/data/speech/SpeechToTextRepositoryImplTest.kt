package com.aitranslator.app.data.speech

import com.aitranslator.app.domain.speech.SpeechToTextEngine
import com.aitranslator.app.domain.translation.Language
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeSpeechToTextEngine : SpeechToTextEngine {
    var lastLanguage: Language? = null
    var result: Result<String> = Result.success("")

    override suspend fun listen(language: Language): Result<String> {
        lastLanguage = language
        return result
    }
}

class SpeechToTextRepositoryImplTest {

    @Test
    fun `listen forwards the language to the engine`() = runTest {
        val engine = FakeSpeechToTextEngine()
        engine.result = Result.success("Hola mundo")
        val repository = SpeechToTextRepositoryImpl(engine)

        val result = repository.listen(Language.SPANISH)

        assertEquals(Language.SPANISH, engine.lastLanguage)
        assertEquals("Hola mundo", result.getOrNull())
    }

    @Test
    fun `engine failure is propagated`() = runTest {
        val engine = FakeSpeechToTextEngine()
        engine.result = Result.failure(SpeechToTextException("No match"))
        val repository = SpeechToTextRepositoryImpl(engine)

        val result = repository.listen(Language.ENGLISH)

        assertTrue(result.isFailure)
    }
}