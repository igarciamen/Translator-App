package com.aitranslator.app.domain.dictionary

import com.aitranslator.app.domain.translation.Language

interface DictionaryRepository {
    suspend fun lookup(word: String, language: Language): Result<DictionaryEntry>
}