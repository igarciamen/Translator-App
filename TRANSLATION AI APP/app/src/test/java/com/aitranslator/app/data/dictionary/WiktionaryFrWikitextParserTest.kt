package com.aitranslator.app.data.dictionary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WiktionaryFrWikitextParserTest {

    // Modeled on the real wikitext for "chien" (fr.wiktionary.org),
    // including Noun and Adjective sections (2 senses each is trimmed to
    // 2 + 1), plus an Old French ("fro") section to verify language
    // isolation, since French Wiktionary lists every language's entry on
    // the same page. Deliberately includes both parameter orders of
    // {{exemple|...}} ("lang=fr" first vs. last) seen in the real data.
    private val sampleWikitext = """
        == {{langue|fr}} ==
        === {{S|nom|fr}} ===
        '''chien''' {{pron|ʃjɛ̃|fr}} {{m}}
        # [[mammifère|Mammifère]] carnivore de la famille des Canidés, domestiqué par l’être humain.
        #* {{exemple | lang=fr
         | Le chien s’était mis à roder dans les environs.
         | source=Exemple, 1900}}
        # Personne rude ou sévère, avare.
        #* {{exemple|C'est un chien avec son argent.|lang=fr}}

        === {{S|adjectif|fr}} ===
        '''chien''' {{pron|ʃjɛ̃|fr}}
        # Avare ; radin ; très près de ses sous.
        #* {{exemple|Il n'est pas chien du tout.|lang=fr}}

        == {{langue|fro}} ==
        === {{S|nom|fro}} ===
        '''chien''' {{pron-recons|tʃjẽn|fro}} {{m}}
        # Chien.
    """.trimIndent()

    @Test
    fun `parseDefinitions extracts senses from both Noun and Adjective sections`() {
        val definitions = WiktionaryFrWikitextParser.parseDefinitions(sampleWikitext)

        assertEquals(3, definitions.size)
        assertTrue(definitions[0].text.contains("Mammifère"))
        assertTrue(definitions[1].text.contains("Personne rude"))
        assertTrue(definitions[2].text.contains("Avare"))
    }

    @Test
    fun `parseDefinitions does not leak definitions from the Old French section`() {
        val definitions = WiktionaryFrWikitextParser.parseDefinitions(sampleWikitext)

        assertEquals(3, definitions.size)
    }

    @Test
    fun `parseDefinitions extracts an example regardless of lang-fr parameter position`() {
        val definitions = WiktionaryFrWikitextParser.parseDefinitions(sampleWikitext)

        assertTrue(definitions[0].example?.contains("roder dans les environs") == true)
        assertTrue(definitions[1].example?.contains("chien avec son argent") == true)
        assertTrue(definitions[2].example?.contains("pas chien du tout") == true)
    }

    @Test
    fun `parseDefinitions returns no synonyms, by design, for the French parser`() {
        val definitions = WiktionaryFrWikitextParser.parseDefinitions(sampleWikitext)

        assertTrue(definitions.all { it.synonyms.isEmpty() })
    }

    @Test
    fun `parseDefinitions strips wiki markup from definition text`() {
        val definitions = WiktionaryFrWikitextParser.parseDefinitions(sampleWikitext)

        assertTrue(!definitions[0].text.contains("[["))
    }

    @Test
    fun `parsePronunciation extracts the IPA value from the fr pron template`() {
        assertEquals("ʃjɛ̃", WiktionaryFrWikitextParser.parsePronunciation(sampleWikitext))
    }

    @Test
    fun `parseDefinitions returns empty list when the French section is absent`() {
        val definitions = WiktionaryFrWikitextParser.parseDefinitions("== {{langue|fro}} ==\n# Chien.")

        assertTrue(definitions.isEmpty())
    }
}