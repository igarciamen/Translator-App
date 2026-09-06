package com.aitranslator.app.data.translation

import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.domain.translation.LanguageDetector
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeLanguageDetector : LanguageDetector {
    var lastInput: String? = null
    var result: Result<Language> = Result.success(Language.ENGLISH)

    override suspend fun detectLanguage(text: String): Result<Language> {
        lastInput = text
        return result
    }

    override fun close() = Unit
}

class LanguageDetectionRepositoryImplTest {

    @Test
    fun `blank text fails without reaching the detector`() = runTest {
        val detector = FakeLanguageDetector()
        val repository = LanguageDetectionRepositoryImpl(detector)

        val result = repository.detectLanguage("   ")

        assertTrue(result.isFailure)
        assertEquals(null, detector.lastInput)
    }

    @Test
    fun `non-blank text is forwarded to the detector`() = runTest {
        val detector = FakeLanguageDetector()
        detector.result = Result.success(Language.FRENCH)
        val repository = LanguageDetectionRepositoryImpl(detector)

        val result = repository.detectLanguage("Bonjour le monde")

        assertEquals("Bonjour le monde", detector.lastInput)
        assertEquals(Language.FRENCH, result.getOrNull())
    }

    @Test
    fun `detector failure is propagated`() = runTest {
        val detector = FakeLanguageDetector()
        detector.result = Result.failure(LanguageDetectionException("Could not determine the language of the text"))
        val repository = LanguageDetectionRepositoryImpl(detector)

        val result = repository.detectLanguage("???")

        assertTrue(result.isFailure)
    }
}