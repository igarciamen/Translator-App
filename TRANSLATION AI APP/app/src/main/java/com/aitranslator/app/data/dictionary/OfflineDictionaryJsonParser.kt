package com.aitranslator.app.data.dictionary

import com.aitranslator.app.domain.dictionary.DictionaryDefinition
import com.aitranslator.app.domain.dictionary.DictionaryEntry
import org.json.JSONObject

/**
 * Parses the `data` column of each bundled offline dictionary into the
 * shared DictionaryEntry/DictionaryDefinition domain model — the same
 * model used by the online Wiktionary-backed repositories, so the UI
 * doesn't need to know or care whether a result came from a network
 * lookup or a local database.
 *
 * Each language's dictionary was independently generated and uses its
 * own JSON shape, confirmed against real entries for "perro" (es),
 * "dog" (en) and "chien" (fr) before writing this parser:
 * - Spanish:  {"definitions":[{"definition":"...","pos":"..."}], "synonyms":[...]}
 *             (synonyms are for the word as a whole, not per definition)
 * - English:  {"senses":[{"pos":"...","definition":"...","examples":[...],"synonyms":[...]}]}
 *             (examples/synonyms are attached per individual sense)
 * - French:   {"pronunciation":"...","grammar":"...","definitions":["...", "..."]}
 *             (definitions are plain strings, no per-definition examples/synonyms)
 */
object OfflineDictionaryJsonParser {

    fun parseSpanish(word: String, rawJson: String): DictionaryEntry {
        val root = JSONObject(rawJson)
        val wordLevelSynonyms = root.optJSONArray("synonyms")?.let { array ->
            (0 until array.length()).map { array.getString(it) }
        }.orEmpty()

        val definitionsArray = root.optJSONArray("definitions")
        val definitions = if (definitionsArray != null) {
            (0 until definitionsArray.length()).map { index ->
                val entry = definitionsArray.getJSONObject(index)
                DictionaryDefinition(
                    text = entry.optString("definition"),
                    synonyms = if (index == 0) wordLevelSynonyms else emptyList()
                )
            }
        } else {
            emptyList()
        }

        return DictionaryEntry(word = word, definitions = definitions)
    }

    fun parseEnglish(word: String, rawJson: String): DictionaryEntry {
        val root = JSONObject(rawJson)
        val sensesArray = root.optJSONArray("senses")

        val definitions = if (sensesArray != null) {
            (0 until sensesArray.length()).map { index ->
                val sense = sensesArray.getJSONObject(index)
                val examples = sense.optJSONArray("examples")
                val synonyms = sense.optJSONArray("synonyms")

                DictionaryDefinition(
                    text = sense.optString("definition"),
                    synonyms = synonyms?.let { array ->
                        (0 until array.length()).map { array.getString(it) }
                    }.orEmpty(),
                    example = examples?.takeIf { it.length() > 0 }?.getString(0)
                )
            }
        } else {
            emptyList()
        }

        return DictionaryEntry(word = word, definitions = definitions)
    }

    fun parseFrench(word: String, rawJson: String): DictionaryEntry {
        val root = JSONObject(rawJson)
        val pronunciation = root.optString("pronunciation").takeIf { it.isNotBlank() }
        val definitionsArray = root.optJSONArray("definitions")

        val definitions = if (definitionsArray != null) {
            (0 until definitionsArray.length()).map { index ->
                DictionaryDefinition(text = definitionsArray.getString(index))
            }
        } else {
            emptyList()
        }

        return DictionaryEntry(word = word, pronunciation = pronunciation, definitions = definitions)
    }
}