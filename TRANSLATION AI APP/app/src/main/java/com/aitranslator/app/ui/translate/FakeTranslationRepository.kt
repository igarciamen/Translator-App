package com.aitranslator.app.ui.translate

import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.domain.translation.TranslationRepository

class FakeTranslationRepository : TranslationRepository {

    var result: Result<String> = Result.success("")
    var lastTranslatedText: String? = null
    var lastSourceLanguage: Language? = null
    var lastTargetLanguage: Language? = null

    var downloadedLanguages: MutableSet<Language> = mutableSetOf()
    var downloadResult: Result<Unit> = Result.success(Unit)
    var deleteResult: Result<Unit> = Result.success(Unit)
    var lastDownloadRequiredWifi: Boolean? = null

    override suspend fun translate(
        text: String,
        sourceLanguage: Language,
        targetLanguage: Language
    ): Result<String> {
        lastTranslatedText = text
        lastSourceLanguage = sourceLanguage
        lastTargetLanguage = targetLanguage
        return result
    }

    override suspend fun isModelDownloaded(language: Language): Boolean {
        return language in downloadedLanguages
    }

    override suspend fun downloadModel(language: Language, requireWifi: Boolean): Result<Unit> {
        lastDownloadRequiredWifi = requireWifi
        if (downloadResult.isSuccess) downloadedLanguages.add(language)
        return downloadResult
    }

    override suspend fun deleteModel(language: Language): Result<Unit> {
        if (deleteResult.isSuccess) downloadedLanguages.remove(language)
        return deleteResult
    }

    override suspend fun getDownloadedLanguages(): Set<Language> {
        return downloadedLanguages
    }
}