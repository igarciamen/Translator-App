package com.aitranslator.app.data.dictionary

import com.aitranslator.app.domain.dictionary.DictionaryDefinition

/**
 * Pure wikitext-parsing logic, extracted out of WiktionaryEsRepository so
 * it can be unit tested against real wikitext samples without a network
 * call. Both bugs found in this dictionary feature so far (an HTML
 * references list mistaken for definitions, then a wrong assumption about
 * where the colon sits after a definition number) were only caught by
 * slow manual on-device testing — pulling the pure text logic out here
 * lets the same kind of bug be caught by a fast local test instead.
 */
object WiktionaryWikitextParser {

    // A definition number can be followed directly by ':' (";2: texto")
    // or by extra markup like a semantic category template before the
    // colon (";1 {{csem|mamíferos|perros}}: texto") — [^:\n]* absorbs
    // whatever appears in between, up to the first colon on the line.
    private val definitionLineRegex = Regex("""^;\s*\d+[^:\n]*:\s*(.+)$""", RegexOption.MULTILINE)
    private val synonymsTemplateRegex = Regex("""\{\{sin[oó]nimos?\|([^}]*)\}\}""")
    private val exampleTemplateRegex = Regex("""\{\{ejemplo\|([^}]*)\}\}""")
    private val pronunciationParamRegex = Regex("""fone\d*\s*=\s*([^|}]+)""")

    fun parseDefinitions(wikitext: String): List<DictionaryDefinition> {
        val matches = definitionLineRegex.findAll(wikitext).toList()
        if (matches.isEmpty()) return emptyList()

        // Each definition "owns" the wikitext between its own ";N" line
        // and the next one (or the end of the article) — that's where its
        // {{sinónimos|...}} and {{ejemplo|...}} templates live.
        return matches.mapIndexed { index, match ->
            val blockStart = match.range.first
            val blockEnd = if (index + 1 < matches.size) matches[index + 1].range.first else wikitext.length
            val block = wikitext.substring(blockStart, blockEnd)

            DictionaryDefinition(
                text = cleanWikitext(match.groupValues[1]),
                synonyms = extractSynonyms(block),
                example = extractExample(block)
            )
        }
    }

    fun parsePronunciation(wikitext: String): String? {
        return pronunciationParamRegex.find(wikitext)
            ?.groupValues?.get(1)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun extractSynonyms(block: String): List<String> {
        val match = synonymsTemplateRegex.find(block) ?: return emptyList()
        return match.groupValues[1]
            .split("|")
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.contains("=") } // drop nota=... metadata params
    }

    private fun extractExample(block: String): String? {
        val match = exampleTemplateRegex.find(block) ?: return null
        val firstArg = match.groupValues[1].split("|").firstOrNull()?.trim()
        return firstArg?.let { cleanWikitext(it) }?.takeIf { it.isNotBlank() }
    }

    private fun cleanWikitext(text: String): String {
        return text
            // {{plm|palabra}} displays "palabra" inline as part of the
            // definition (Wiktionary's way of marking a term within a
            // gloss) — it must be replaced with its argument, not deleted
            // outright like a purely decorative/categorization template.
            .replace(Regex("""\{\{plm\|([^|}]+)[^}]*\}\}"""), "$1")
            .replace(Regex("""\{\{[^}]*\}\}"""), "") // remaining templates (categories, etc.)
            .replace(Regex("""\[\[([^|\]]*\|)?([^\]]*)\]\]"""), "$2") // [[link|label]] -> label
            .replace(Regex("""'{2,3}"""), "") // ''italics''/'''bold'''
            .replace(Regex("""<[^>]*>"""), "") // stray HTML tags
            .trim()
    }
}