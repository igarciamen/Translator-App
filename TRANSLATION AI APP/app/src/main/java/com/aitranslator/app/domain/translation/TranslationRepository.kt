package com.aitranslator.app.domain.translation

interface TranslationRepository {
    suspend fun translate(
        text: String,
        sourceLanguage: Language,
        targetLanguage: Language
    ): Result<String>

    suspend fun isModelDownloaded(language: Language): Boolean

    suspend fun downloadModel(language: Language, requireWifi: Boolean = true): Result<Unit>

    suspend fun deleteModel(language: Language): Result<Unit>

    suspend fun getDownloadedLanguages(): Set<Language>
}