package com.aitranslator.app.data.dictionary

import com.aitranslator.app.domain.dictionary.DictionaryErrorType
import com.aitranslator.app.domain.translation.Language
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * WiktionaryEsRepository performs a real HTTP request via OkHttp, so a full
 * network round-trip isn't practical to unit test with plain JUnit (same
 * reasoning applied to other network/hardware-backed repositories in this
 * project). This test covers the one piece of pure logic available without
 * a network call: rejecting unsupported languages before ever reaching the
 * network layer. Wikitext parsing logic itself is covered separately in
 * WiktionaryWikitextParserTest, against real wikitext samples.
 */
class WiktionaryEsRepositoryTest {

    @Test
    fun `lookup rejects languages other than Spanish without making a network call`() = runTest {
        val repository = WiktionaryEsRepository(OkHttpClient())

        val result = repository.lookup("dog", Language.ENGLISH)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as? DictionaryLookupException
        assertTrue(error?.message?.contains("only available for Spanish") == true)
        assertEquals(DictionaryErrorType.UNSUPPORTED_LANGUAGE, error?.errorType)
    }
}