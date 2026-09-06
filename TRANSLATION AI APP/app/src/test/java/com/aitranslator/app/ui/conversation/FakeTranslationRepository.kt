package com.aitranslator.app.ui.conversation

import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.domain.translation.TranslationRepository

class FakeTranslationRepository : TranslationRepository {
    var lastText: String? = null
    var lastSourceLanguage: Language? = null
    var lastTargetLanguage: Language? = null
    var result: Result<String> = Result.success("")

    override suspend fun translate(text: String, sourceLanguage: Language, targetLanguage: Language): Result<String> {
        lastText = text
        lastSourceLanguage = sourceLanguage
        lastTargetLanguage = targetLanguage
        return result
    }

    override suspend fun isModelDownloaded(language: Language): Boolean = true

    override suspend fun downloadModel(language: Language, requireWifi: Boolean): Result<Unit> = Result.success(Unit)

    override suspend fun deleteModel(language: Language): Result<Unit> = Result.success(Unit)

    override suspend fun getDownloadedLanguages(): Set<Language> = emptySet()
}