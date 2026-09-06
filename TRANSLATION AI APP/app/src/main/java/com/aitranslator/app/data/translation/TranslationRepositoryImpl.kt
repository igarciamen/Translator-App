package com.aitranslator.app.data.translation

import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.domain.translation.TranslationEngine
import com.aitranslator.app.domain.translation.TranslationRepository
import javax.inject.Inject

class TranslationRepositoryImpl @Inject constructor(
    private val engine: TranslationEngine
) : TranslationRepository {

    override suspend fun translate(
        text: String,
        sourceLanguage: Language,
        targetLanguage: Language
    ): Result<String> {
        if (text.isBlank()) return Result.success("")
        return engine.translate(text, sourceLanguage, targetLanguage)
    }

    override suspend fun isModelDownloaded(language: Language): Boolean {
        return engine.isModelDownloaded(language)
    }

    override suspend fun downloadModel(language: Language, requireWifi: Boolean): Result<Unit> {
        return engine.downloadModel(language, requireWifi)
    }

    override suspend fun deleteModel(language: Language): Result<Unit> {
        return engine.deleteModel(language)
    }

    override suspend fun getDownloadedLanguages(): Set<Language> {
        return engine.getDownloadedLanguages()
    }
}