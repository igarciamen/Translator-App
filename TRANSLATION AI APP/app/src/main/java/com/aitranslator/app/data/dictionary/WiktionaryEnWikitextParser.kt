package com.aitranslator.app.data.dictionary

import com.aitranslator.app.domain.dictionary.DictionaryDefinition

/**
 * Pure wikitext-parsing logic for English Wiktionary pages, mirroring the
 * separation used for WiktionaryWikitextParser (the Spanish parser) so it
 * can be unit tested against a real wikitext sample without a network call.
 *
 * Scope of this first version, deliberately reduced from what the Spanish
 * parser does (confirmed against a real "dog" wikitext sample, which
 * turned out structurally much richer than Spanish's):
 * - Only top-level "# " senses are extracted; nested "##"/"###" sub-senses
 *   are not (a "# A mammal..." definition that introduces a nested list
 *   may end with a trailing ':' as a result — a known, accepted cosmetic
 *   quirk rather than a bug).
 * - Synonyms are not extracted. Unlike Spanish, where {{sinónimos|...}}
 *   sits right under its own definition, English Wiktionary lists all of
 *   a word's synonyms together under one "=====Synonyms=====" heading,
 *   tagged with {{sense|...}} labels — reliably matching a synonym back
 *   to the exact sense it belongs to is failure-prone, and showing a
 *   wrong synonym would be worse than showing none.
 * - Only the first IPA pronunciation found is used, regardless of accent
 *   (English Wiktionary lists several, e.g. RP vs GA).
 * - Every language's entry lives on the same page under its own
 *   "==LanguageName==" heading, so the "==English==" section must be
 *   isolated first or unrelated languages' definitions would leak in.
 */
object WiktionaryEnWikitextParser {

    private val englishSectionRegex = Regex("""(?s)==English==\r?\n(.*?)(?=\r?\n==[^=]|\z)""")

    // A bare "# " starts a top-level sense. Requiring the character right
    // after '#' to be whitespace excludes "##" (nested sub-senses, whose
    // second character is '#' not whitespace), "#:" (examples/relations)
    // and "#*" (quotations).
    private val definitionLineRegex = Regex("""^#\s+(.+)$""", RegexOption.MULTILINE)

    // Matches templates used specifically for usage examples
    // ({{ux|en|...}}, {{uxi|en|...}}, {{usex|en|...}}) — not the "#:"
    // lines used for {{syn|...}}, {{hyper|...}}, etc., which also start
    // with "#:" but aren't examples.
    private val exampleTemplateRegex = Regex("""\{\{(?:ux|uxi|usex)\|en\|([^|}]+)""")

    private val ipaRegex = Regex("""\{\{IPA\|en\|([^|}]+)""")
    private val refBlockRegex = Regex("""(?s)<ref[^>]*>.*?</ref>|<ref[^>]*/>""")

    fun parseDefinitions(wikitext: String): List<DictionaryDefinition> {
        val englishSection = extractEnglishSection(wikitext) ?: return emptyList()
        val matches = definitionLineRegex.findAll(englishSection).toList()
        if (matches.isEmpty()) return emptyList()

        return matches.mapIndexed { index, match ->
            val blockStart = match.range.first
            val blockEnd = if (index + 1 < matches.size) matches[index + 1].range.first else englishSection.length
            val block = englishSection.substring(blockStart, blockEnd)

            DictionaryDefinition(
                text = cleanWikitext(match.groupValues[1]),
                synonyms = emptyList(),
                example = extractExample(block)
            )
        }
    }

    fun parsePronunciation(wikitext: String): String? {
        val englishSection = extractEnglishSection(wikitext) ?: return null
        return ipaRegex.find(englishSection)
            ?.groupValues?.get(1)
            ?.trim()
            ?.trim('/')
            ?.takeIf { it.isNotBlank() }
    }

    private fun extractEnglishSection(wikitext: String): String? {
        return englishSectionRegex.find(wikitext)?.groupValues?.get(1)
    }

    private fun extractExample(block: String): String? {
        val match = exampleTemplateRegex.find(block) ?: return null
        return cleanWikitext(match.groupValues[1]).takeIf { it.isNotBlank() }
    }

    private fun cleanWikitext(text: String): String {
        return text
            .replace(refBlockRegex, "")
            .replace(Regex("""\{\{[^{}]*\}\}"""), "")
            .replace(Regex("""\[\[([^|\]]*\|)?([^\]]*)\]\]"""), "$2")
            .replace(Regex("""'{2,3}"""), "")
            .replace(Regex("""<[^>]*>"""), "")
            .trim()
    }
}