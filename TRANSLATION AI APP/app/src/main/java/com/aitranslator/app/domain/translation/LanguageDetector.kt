package com.aitranslator.app.domain.translation

interface LanguageDetector {
    suspend fun detectLanguage(text: String): Result<Language>
    fun close()
}