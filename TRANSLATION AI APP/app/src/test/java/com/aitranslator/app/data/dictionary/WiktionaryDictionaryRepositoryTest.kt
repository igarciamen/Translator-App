package com.aitranslator.app.data.dictionary

import com.aitranslator.app.domain.dictionary.DictionaryErrorType
import com.aitranslator.app.domain.translation.Language
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WiktionaryDictionaryRepositoryTest {

    @Test
    fun `lookup rejects a language with no per-language repository, without a network call`() = runTest {
        val client = OkHttpClient()
        val repository = WiktionaryDictionaryRepository(
            WiktionaryEsRepository(client),
            WiktionaryEnRepository(client),
            WiktionaryFrRepository(client)
        )

        val result = repository.lookup("Hund", Language.GERMAN)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as? DictionaryLookupException
        assertEquals(DictionaryErrorType.UNSUPPORTED_LANGUAGE, error?.errorType)
    }
}