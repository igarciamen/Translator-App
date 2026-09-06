package com.aitranslator.app.data.dictionary

import com.aitranslator.app.domain.dictionary.DictionaryEntry
import com.aitranslator.app.domain.dictionary.DictionaryErrorType
import com.aitranslator.app.domain.dictionary.DictionaryRepository
import com.aitranslator.app.domain.translation.Language
import javax.inject.Inject

/**
 * Looks up word definitions from SQLite databases bundled as app assets
 * (see OfflineDictionaryDatabaseAccess), fully offline. Each supported
 * language has its own bundled database and its own JSON shape inside
 * that database, handled by OfflineDictionaryJsonParser.
 */
class OfflineDictionaryRepository @Inject constructor(
    private val databaseAccess: OfflineDictionaryDatabaseAccess
) : DictionaryRepository {

    override suspend fun lookup(word: String, language: Language): Result<DictionaryEntry> {
        val assetFileName = assetFileNameFor(language)
            ?: return Result.failure(
                DictionaryLookupException(
                    "Offline dictionary lookup is only available for Spanish, English and French",
                    DictionaryErrorType.UNSUPPORTED_LANGUAGE
                )
            )

        return try {
            val rawJson = databaseAccess.findRawEntry(assetFileName, word.trim())
                ?: return Result.failure(
                    DictionaryLookupException("This word was not found", DictionaryErrorType.WORD_NOT_FOUND)
                )

            val entry = when (language) {
                Language.SPANISH -> OfflineDictionaryJsonParser.parseSpanish(word.trim(), rawJson)
                Language.ENGLISH -> OfflineDictionaryJsonParser.parseEnglish(word.trim(), rawJson)
                Language.FRENCH -> OfflineDictionaryJsonParser.parseFrench(word.trim(), rawJson)
                else -> return Result.failure(
                    DictionaryLookupException(
                        "Offline dictionary lookup is only available for Spanish, English and French",
                        DictionaryErrorType.UNSUPPORTED_LANGUAGE
                    )
                )
            }

            if (entry.definitions.isEmpty()) {
                Result.failure(
                    DictionaryLookupException("No definition was found for this word", DictionaryErrorType.WORD_NOT_FOUND)
                )
            } else {
                Result.success(entry)
            }
        } catch (error: Exception) {
            Result.failure(
                DictionaryLookupException(error.message ?: "Offline dictionary lookup failed", DictionaryErrorType.UNKNOWN)
            )
        }
    }

    private fun assetFileNameFor(language: Language): String? = when (language) {
        Language.SPANISH -> "diccionario_espanol.sqlite"
        Language.ENGLISH -> "diccionario_ingles.sqlite"
        Language.FRENCH -> "diccionario_frances.sqlite"
        else -> null
    }
}