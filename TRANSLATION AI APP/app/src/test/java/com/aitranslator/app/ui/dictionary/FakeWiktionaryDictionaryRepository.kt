package com.aitranslator.app.ui.dictionary

import com.aitranslator.app.domain.dictionary.DictionaryEntry
import com.aitranslator.app.domain.dictionary.DictionaryRepository
import com.aitranslator.app.domain.translation.Language

class FakeWiktionaryDictionaryRepository : DictionaryRepository {
    var lastWord: String? = null
    var lastLanguage: Language? = null
    var result: Result<DictionaryEntry> = Result.success(DictionaryEntry(word = "", definitions = emptyList()))

    override suspend fun lookup(word: String, language: Language): Result<DictionaryEntry> {
        lastWord = word
        lastLanguage = language
        return result
    }
}