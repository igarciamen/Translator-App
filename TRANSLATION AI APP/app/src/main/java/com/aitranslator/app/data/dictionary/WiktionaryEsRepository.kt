package com.aitranslator.app.data.dictionary

import android.util.Log
import com.aitranslator.app.domain.dictionary.DictionaryEntry
import com.aitranslator.app.domain.dictionary.DictionaryErrorType
import com.aitranslator.app.domain.dictionary.DictionaryRepository
import com.aitranslator.app.domain.translation.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

/**
 * Looks up Spanish word definitions using the official MediaWiki Action
 * API (the same API Wikipedia/Wiktionary/Wikidata run on), rather than
 * scraping the rendered HTML page. This avoids the fragility of HTML
 * scraping: the page's visual structure can change at any time without
 * notice, silently breaking a scraper, while the API is versioned,
 * documented, and has been stable for over a decade.
 *
 * We request the raw wikitext source (rvprop=content) rather than parsed
 * HTML. Wikitext parsing itself lives in WiktionaryWikitextParser, so it
 * can be unit tested against real wikitext samples without a network
 * call. Only Spanish is supported in this block for the same reason
 * noted before: each language edition's wikitext conventions differ.
 *
 * Errors are tagged with a DictionaryErrorType so the UI can show a
 * tailored message per failure kind (and only offer a "retry" action for
 * genuinely transient failures like NETWORK_ERROR, not for a word that
 * simply doesn't exist).
 */
class WiktionaryEsRepository @Inject constructor(
    private val httpClient: OkHttpClient
) : DictionaryRepository {

    override suspend fun lookup(word: String, language: Language): Result<DictionaryEntry> {
        if (language != Language.SPANISH) {
            return Result.failure(
                DictionaryLookupException(
                    "Online dictionary lookup is only available for Spanish in this version",
                    DictionaryErrorType.UNSUPPORTED_LANGUAGE
                )
            )
        }

        return withContext(Dispatchers.IO) {
            try {
                val url = "https://es.wiktionary.org/w/api.php".toHttpUrl().newBuilder()
                    .addQueryParameter("action", "query")
                    .addQueryParameter("prop", "revisions")
                    .addQueryParameter("rvprop", "content")
                    .addQueryParameter("rvslots", "main")
                    .addQueryParameter("format", "json")
                    .addQueryParameter("titles", word.trim())
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "AiTranslator-Android-App")
                    .build()

                val response = try {
                    httpClient.newCall(request).execute()
                } catch (error: IOException) {
                    // Thrown by OkHttp for connectivity failures (no
                    // network, DNS failure, timeout, etc.) — distinct
                    // from a successful-but-unhelpful HTTP response.
                    return@withContext Result.failure(
                        DictionaryLookupException(
                            "No internet connection. Check your connection and try again",
                            DictionaryErrorType.NETWORK_ERROR
                        )
                    )
                }

                if (!response.isSuccessful) {
                    response.close()
                    return@withContext Result.failure(
                        DictionaryLookupException(
                            "The dictionary service is unreachable",
                            DictionaryErrorType.NETWORK_ERROR
                        )
                    )
                }

                val bodyText = response.body?.string().orEmpty()
                response.close()

                val wikitext = extractWikitext(bodyText)
                    ?: return@withContext Result.failure(
                        DictionaryLookupException(
                            "This word was not found",
                            DictionaryErrorType.WORD_NOT_FOUND
                        )
                    )

                val definitions = WiktionaryWikitextParser.parseDefinitions(wikitext)
                val pronunciation = WiktionaryWikitextParser.parsePronunciation(wikitext)

                Log.d("DictionaryLookup", "Definitions parsed: ${definitions.size}, pronunciation=$pronunciation")

                if (definitions.isEmpty()) {
                    Result.failure(
                        DictionaryLookupException(
                            "No definition was found for this word",
                            DictionaryErrorType.WORD_NOT_FOUND
                        )
                    )
                } else {
                    Result.success(
                        DictionaryEntry(
                            word = word.trim(),
                            pronunciation = pronunciation,
                            definitions = definitions
                        )
                    )
                }
            } catch (error: Exception) {
                Log.e("DictionaryLookup", "Exception during lookup", error)
                Result.failure(
                    DictionaryLookupException(
                        error.message ?: "Dictionary lookup failed",
                        DictionaryErrorType.UNKNOWN
                    )
                )
            }
        }
    }

    /**
     * Navigates the JSON response shape returned by action=query&prop=revisions:
     * query.pages.{pageId}.revisions[0].slots.main.<contentKey>
     * A page ID of "-1" (missing page) means the word doesn't exist.
     *
     * We do NOT hardcode the content key ("*" vs "content"): MediaWiki
     * nests the wikitext inside slots.main under a key whose exact name
     * is easy to assume wrong. Instead, we read mainSlot's keys directly
     * at runtime and pick the one that isn't a known metadata key
     * ("contentmodel", "contentformat") — correct regardless of which
     * key MediaWiki happens to use.
     */
    private fun extractWikitext(responseBody: String): String? {
        val root = JSONObject(responseBody)
        val pages = root.optJSONObject("query")?.optJSONObject("pages") ?: return null
        val pageId = pages.keys().asSequence().firstOrNull() ?: return null
        if (pageId == "-1") return null

        val page = pages.optJSONObject(pageId) ?: return null
        val revisions = page.optJSONArray("revisions") ?: return null
        if (revisions.length() == 0) return null

        val firstRevision = revisions.optJSONObject(0) ?: return null
        val slots = firstRevision.optJSONObject("slots")
        val mainSlot = slots?.optJSONObject("main")

        if (mainSlot != null) {
            val metadataKeys = setOf("contentmodel", "contentformat")
            val contentKey = mainSlot.keys().asSequence().firstOrNull { it !in metadataKeys }
            val fromSlot = contentKey?.let { mainSlot.optString(it) }?.takeIf { it.isNotBlank() }
            if (fromSlot != null) return fromSlot
        }

        // Fallback for the legacy (pre-slots) response shape, where the
        // wikitext sits directly on the revision object under "*".
        return firstRevision.optString("*").takeIf { it.isNotBlank() }
    }
}