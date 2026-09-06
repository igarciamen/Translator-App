package com.aitranslator.app.data.dictionary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WiktionaryEnWikitextParserTest {

    // Trimmed excerpt loosely modeled on the real wikitext for "dog"
    // (en.wiktionary.org), including a second language section
    // (Afrikaans) to verify language-section isolation, since English
    // Wiktionary pages list every language's entry on the same page.
    private val sampleWikitext = """
        ==English==

        ===Pronunciation===
        * {{IPA|en|/dɒɡ/|a=RP}}
        * {{enPR|dôg|a=GA}}

        ====Noun====
        {{en-noun|~}}

        # A [[mammal]] of the [[family]] {{taxfmt|Canidae|family}}:
        ## The [[species]] {{taxfmt|Canis familiaris|species}}, domesticated for thousands of years.
        ##: {{ux|en|The '''dog''' barked all night long.}}
        # {{lb|en|uncountable|rare}} The [[meat]] of this animal, eaten as food.
        #: {{usex|en|Did you know that they eat '''dog''' in parts of Asia?}}

        ====Verb====
        {{en-verb}}

        # {{lb|en|transitive}} To [[pursue]] with the [[intent]] to [[catch]].
        #: {{syn|en|chase|chase after}}

        ==Afrikaans==

        ===Verb===

        # {{alt form of|af|dag}}
    """.trimIndent()

    @Test
    fun `parseDefinitions only extracts top-level senses from the English section`() {
        val definitions = WiktionaryEnWikitextParser.parseDefinitions(sampleWikitext)

        assertEquals(3, definitions.size)
        assertTrue(definitions[0].text.contains("mammal"))
        assertTrue(definitions[1].text.contains("meat"))
        assertTrue(definitions[2].text.contains("pursue"))
    }

    @Test
    fun `parseDefinitions does not leak definitions from other languages on the same page`() {
        val definitions = WiktionaryEnWikitextParser.parseDefinitions(sampleWikitext)

        assertTrue(definitions.none { it.text.contains("dag") })
    }

    @Test
    fun `parseDefinitions extracts an example when a ux-style template is present`() {
        val definitions = WiktionaryEnWikitextParser.parseDefinitions(sampleWikitext)

        assertTrue(definitions[1].example?.contains("eat dog in parts of Asia") == true)
    }

    @Test
    fun `parseDefinitions leaves example null when only a synonym template is present`() {
        val definitions = WiktionaryEnWikitextParser.parseDefinitions(sampleWikitext)

        assertNull(definitions[2].example)
    }

    @Test
    fun `parseDefinitions returns no synonyms, by design, for the English parser`() {
        val definitions = WiktionaryEnWikitextParser.parseDefinitions(sampleWikitext)

        assertTrue(definitions.all { it.synonyms.isEmpty() })
    }

    @Test
    fun `parseDefinitions strips wiki markup from definition text`() {
        val definitions = WiktionaryEnWikitextParser.parseDefinitions(sampleWikitext)

        assertTrue(!definitions[0].text.contains("[["))
    }

    @Test
    fun `parsePronunciation extracts the first IPA value, without slashes`() {
        assertEquals("dɒɡ", WiktionaryEnWikitextParser.parsePronunciation(sampleWikitext))
    }

    @Test
    fun `parseDefinitions returns empty list when the language section is absent`() {
        val definitions = WiktionaryEnWikitextParser.parseDefinitions("==Afrikaans==\n# {{alt form of|af|dag}}")

        assertTrue(definitions.isEmpty())
    }
}