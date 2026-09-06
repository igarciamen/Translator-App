package com.aitranslator.app.domain.translation

interface LanguageDetectionRepository {
    suspend fun detectLanguage(text: String): Result<Language>
}