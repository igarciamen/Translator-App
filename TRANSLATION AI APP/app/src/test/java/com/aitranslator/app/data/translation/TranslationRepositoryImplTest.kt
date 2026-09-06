package com.aitranslator.app.data.translation

import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.domain.translation.TranslationEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeTranslationEngine : TranslationEngine {
    var lastInput: String? = null
    var lastSource: Language? = null
    var lastTarget: Language? = null
    var result: Result<String> = Result.success("")

    var downloadedLanguages: MutableSet<Language> = mutableSetOf()
    var downloadResult: Result<Unit> = Result.success(Unit)
    var deleteResult: Result<Unit> = Result.success(Unit)
    var lastRequireWifi: Boolean? = null
    var lastDeletedLanguage: Language? = null

    override suspend fun translate(
        text: String,
        sourceLanguage: Language,
        targetLanguage: Language
    ): Result<String> {
        lastInput = text
        lastSource = sourceLanguage
        lastTarget = targetLanguage
        return result
    }

    override suspend fun isModelDownloaded(language: Language): Boolean {
        return language in downloadedLanguages
    }

    override suspend fun downloadModel(language: Language, requireWifi: Boolean): Result<Unit> {
        lastRequireWifi = requireWifi
        if (downloadResult.isSuccess) downloadedLanguages.add(language)
        return downloadResult
    }

    override suspend fun deleteModel(language: Language): Result<Unit> {
        lastDeletedLanguage = language
        if (deleteResult.isSuccess) downloadedLanguages.remove(language)
        return deleteResult
    }

    override suspend fun getDownloadedLanguages(): Set<Language> {
        return downloadedLanguages
    }

    override fun close() = Unit
}

class TranslationRepositoryImplTest {

    @Test
    fun `blank text short-circuits and never reaches the engine`() = runTest {
        val engine = FakeTranslationEngine()
        val repository = TranslationRepositoryImpl(engine)

        val result = repository.translate("   ", Language.SPANISH, Language.ENGLISH)

        assertTrue(result.isSuccess)
        assertEquals("", result.getOrNull())
        assertEquals(null, engine.lastInput)
    }

    @Test
    fun `non-blank text and languages are forwarded to the engine`() = runTest {
        val engine = FakeTranslationEngine()
        engine.result = Result.success("Hello")
        val repository = TranslationRepositoryImpl(engine)

        val result = repository.translate("Hola", Language.SPANISH, Language.ENGLISH)

        assertEquals("Hola", engine.lastInput)
        assertEquals(Language.SPANISH, engine.lastSource)
        assertEquals(Language.ENGLISH, engine.lastTarget)
        assertEquals("Hello", result.getOrNull())
    }

    @Test
    fun `engine failure is propagated as a failed result`() = runTest {
        val engine = FakeTranslationEngine()
        engine.result = Result.failure(RuntimeException("boom"))
        val repository = TranslationRepositoryImpl(engine)

        val result = repository.translate("Hola", Language.SPANISH, Language.ENGLISH)

        assertTrue(result.isFailure)
    }

    @Test
    fun `isModelDownloaded delegates to the engine`() = runTest {
        val engine = FakeTranslationEngine()
        engine.downloadedLanguages.add(Language.FRENCH)
        val repository = TranslationRepositoryImpl(engine)

        assertTrue(repository.isModelDownloaded(Language.FRENCH))
        assertFalse(repository.isModelDownloaded(Language.GERMAN))
    }

    @Test
    fun `downloadModel forwards the wifi requirement to the engine`() = runTest {
        val engine = FakeTranslationEngine()
        val repository = TranslationRepositoryImpl(engine)

        repository.downloadModel(Language.ITALIAN, requireWifi = false)

        assertEquals(false, engine.lastRequireWifi)
        assertTrue(Language.ITALIAN in engine.downloadedLanguages)
    }

    @Test
    fun `deleteModel forwards the target language to the engine`() = runTest {
        val engine = FakeTranslationEngine()
        engine.downloadedLanguages.add(Language.PORTUGUESE)
        val repository = TranslationRepositoryImpl(engine)

        repository.deleteModel(Language.PORTUGUESE)

        assertEquals(Language.PORTUGUESE, engine.lastDeletedLanguage)
        assertFalse(Language.PORTUGUESE in engine.downloadedLanguages)
    }

    @Test
    fun `getDownloadedLanguages returns what the engine reports`() = runTest {
        val engine = FakeTranslationEngine()
        engine.downloadedLanguages.addAll(listOf(Language.SPANISH, Language.ENGLISH))
        val repository = TranslationRepositoryImpl(engine)

        val downloaded = repository.getDownloadedLanguages()

        assertEquals(setOf(Language.SPANISH, Language.ENGLISH), downloaded)
    }
}