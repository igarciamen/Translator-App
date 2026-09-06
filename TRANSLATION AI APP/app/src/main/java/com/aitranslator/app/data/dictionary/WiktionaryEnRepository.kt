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
 * Looks up English word definitions using the official MediaWiki Action
 * API against en.wiktionary.org, mirroring WiktionaryEsRepository's
 * network/JSON-navigation approach but delegating wikitext parsing to
 * WiktionaryEnWikitextParser (see its class doc for why English needs a
 * different, reduced-scope parser than Spanish).
 */
class WiktionaryEnRepository @Inject constructor(
    private val httpClient: OkHttpClient
) : DictionaryRepository {

    override suspend fun lookup(word: String, language: Language): Result<DictionaryEntry> {
        if (language != Language.ENGLISH) {
            return Result.failure(
                DictionaryLookupException(
                    "This repository only handles English lookups",
                    DictionaryErrorType.UNSUPPORTED_LANGUAGE
                )
            )
        }

        return withContext(Dispatchers.IO) {
            try {
                val url = "https://en.wiktionary.org/w/api.php".toHttpUrl().newBuilder()
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
                        DictionaryLookupException("This word was not found", DictionaryErrorType.WORD_NOT_FOUND)
                    )

                val definitions = WiktionaryEnWikitextParser.parseDefinitions(wikitext)
                val pronunciation = WiktionaryEnWikitextParser.parsePronunciation(wikitext)

                Log.d("DictionaryLookup", "[en] Definitions parsed: ${definitions.size}, pronunciation=$pronunciation")

                if (definitions.isEmpty()) {
                    Result.failure(
                        DictionaryLookupException("No definition was found for this word", DictionaryErrorType.WORD_NOT_FOUND)
                    )
                } else {
                    Result.success(
                        DictionaryEntry(word = word.trim(), pronunciation = pronunciation, definitions = definitions)
                    )
                }
            } catch (error: Exception) {
                Log.e("DictionaryLookup", "[en] Exception during lookup", error)
                Result.failure(
                    DictionaryLookupException(error.message ?: "Dictionary lookup failed", DictionaryErrorType.UNKNOWN)
                )
            }
        }
    }

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

        return firstRevision.optString("*").takeIf { it.isNotBlank() }
    }
}