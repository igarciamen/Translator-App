package com.aitranslator.app.data.dictionary

import com.aitranslator.app.domain.dictionary.DictionaryDefinition

/**
 * Pure wikitext-parsing logic for French Wiktionary pages, following the
 * same reduced-scope reasoning already applied to WiktionaryEnWikitextParser:
 * synonyms are grouped under a single "==== {{S|synonymes}} ====" heading
 * rather than attached per definition, so they are not extracted here to
 * avoid misassigning one to the wrong sense.
 *
 * Confirmed against a real "chien" wikitext sample: French Wiktionary uses
 * "# " for top-level senses (like English, unlike Spanish's ";N:"), and
 * every language's entry (fr, fro, gallo, ...) lives on the same page
 * under its own "== {{langue|xx}} ==" heading, so "== {{langue|fr}} =="
 * must be isolated first.
 *
 * Example extraction is the trickiest part here: the {{exemple|...}}
 * template's parameters ("lang=fr", the example text, "source=...") do
 * not appear in a fixed order across the page — sometimes lang=fr comes
 * first, sometimes last. Rather than a fixed-position regex, the
 * template's pipe-separated parts are scanned in order, skipping any
 * "lang="/"source=" parts and collecting the rest (rejoined with "|" to
 * restore any pipe that was actually part of a [[link|label]] inside the
 * text) as the example.
 */
object WiktionaryFrWikitextParser {

    private val frenchSectionRegex =
        Regex("""(?s)==\s*\{\{langue\|fr\}\}\s*==\r?\n(.*?)(?=\r?\n==\s*\{\{langue\|[^}]+\}\}\s*==|\z)""")

    private val definitionLineRegex = Regex("""^#\s+(.+)$""", RegexOption.MULTILINE)
    private val pronRegex = Regex("""\{\{pron\|([^|}]+)\|fr\}\}""")

    fun parseDefinitions(wikitext: String): List<DictionaryDefinition> {
        val frenchSection = extractFrenchSection(wikitext) ?: return emptyList()
        val matches = definitionLineRegex.findAll(frenchSection).toList()
        if (matches.isEmpty()) return emptyList()

        return matches.mapIndexed { index, match ->
            val blockStart = match.range.first
            val blockEnd = if (index + 1 < matches.size) matches[index + 1].range.first else frenchSection.length
            val block = frenchSection.substring(blockStart, blockEnd)

            DictionaryDefinition(
                text = cleanWikitext(match.groupValues[1]),
                synonyms = emptyList(),
                example = extractExample(block)
            )
        }
    }

    fun parsePronunciation(wikitext: String): String? {
        val frenchSection = extractFrenchSection(wikitext) ?: return null
        return pronRegex.find(frenchSection)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun extractFrenchSection(wikitext: String): String? {
        return frenchSectionRegex.find(wikitext)?.groupValues?.get(1)
    }

    private fun extractExample(block: String): String? {
        val startIdx = block.indexOf("{{exemple")
        if (startIdx == -1) return null

        // Bounded window: enough to contain a realistic example template,
        // without risking bleeding into unrelated later content if this
        // particular template is unusually large or malformed.
        val window = block.substring(startIdx + "{{exemple".length).take(2000)

        val collected = mutableListOf<String>()
        for (part in window.split("|")) {
            val trimmedPart = part.trim()
            if (trimmedPart.isEmpty()) continue
            if (trimmedPart.startsWith("lang=", ignoreCase = true)) continue
            if (trimmedPart.startsWith("source=", ignoreCase = true)) break
            collected.add(part)
        }
        if (collected.isEmpty()) return null

        val rawText = collected.joinToString("|").trim().removeSuffix("}}").trim()
        return cleanWikitext(rawText).takeIf { it.isNotBlank() }
    }

    private fun cleanWikitext(text: String): String {
        return text
            .replace(Regex("""\{\{[^{}]*\}\}"""), "")
            .replace(Regex("""\[\[([^|\]]*\|)?([^\]]*)\]\]"""), "$2")
            .replace(Regex("""'{2,3}"""), "")
            .replace(Regex("""<[^>]*>"""), "")
            .trim()
    }
}