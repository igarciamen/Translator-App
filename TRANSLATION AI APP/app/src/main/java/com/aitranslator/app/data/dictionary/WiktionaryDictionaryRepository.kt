package com.aitranslator.app.data.dictionary

import com.aitranslator.app.domain.dictionary.DictionaryEntry
import com.aitranslator.app.domain.dictionary.DictionaryErrorType
import com.aitranslator.app.domain.dictionary.DictionaryRepository
import com.aitranslator.app.domain.translation.Language
import javax.inject.Inject

/**
 * Dispatches a lookup to whichever per-language Wiktionary repository
 * knows how to parse that language edition's wikitext conventions — each
 * edition has been found to differ substantially (see each per-language
 * repository's class doc). Adding support for another language means
 * adding another per-language repository + a branch here, without
 * touching the ViewModel or UI at all.
 */
class WiktionaryDictionaryRepository @Inject constructor(
    private val spanishRepository: WiktionaryEsRepository,
    private val englishRepository: WiktionaryEnRepository,
    private val frenchRepository: WiktionaryFrRepository
) : DictionaryRepository {

    override suspend fun lookup(word: String, language: Language): Result<DictionaryEntry> {
        return when (language) {
            Language.SPANISH -> spanishRepository.lookup(word, language)
            Language.ENGLISH -> englishRepository.lookup(word, language)
            Language.FRENCH -> frenchRepository.lookup(word, language)
            else -> Result.failure(
                DictionaryLookupException(
                    "Online dictionary lookup is only available for Spanish, English and French in this version",
                    DictionaryErrorType.UNSUPPORTED_LANGUAGE
                )
            )
        }
    }
}