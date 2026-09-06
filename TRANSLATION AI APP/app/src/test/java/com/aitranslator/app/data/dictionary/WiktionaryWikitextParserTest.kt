package com.aitranslator.app.data.dictionary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WiktionaryWikitextParserTest {

    // Trimmed excerpt of the real wikitext for "perro" (es.wiktionary.org),
    // covering the first two numbered senses.
    private val sampleWikitext = """
        ==== {{sustantivo masculino y femenino|es}} ====
        {{es.sust|mf}}
        ;1 {{csem|mamíferos|perros}}: (''Canis lupus familiaris'') Variedad doméstica del [[lobo]] de muchas y diversas [[raza]]s, compañero del hombre desde tiempos prehistóricos.
        {{sinónimos|chucho|can|tuso}}
        {{ejemplo|Un ''perro'' grande, de erizada pelambre, había atravesado la casa.|a=Alejo Carpentier|c=libro|f=1949}}

        ==== {{sustantivo masculino|es}} ====
        {{es.sust}}
        ;2: {{plm|sándwich}} de [[salchicha de Viena]] en un [[pan]] largo y delgado.
        {{ámbito|Venezuela}}
    """.trimIndent()

    @Test
    fun `parseDefinitions extracts both numbered senses`() {
        val definitions = WiktionaryWikitextParser.parseDefinitions(sampleWikitext)

        assertEquals(2, definitions.size)
        assertTrue(definitions[0].text.contains("Variedad doméstica"))
        assertTrue(definitions[1].text.contains("sándwich"))
    }

    @Test
    fun `parseDefinitions extracts synonyms for the sense that has them`() {
        val definitions = WiktionaryWikitextParser.parseDefinitions(sampleWikitext)

        assertEquals(listOf("chucho", "can", "tuso"), definitions[0].synonyms)
        assertTrue(definitions[1].synonyms.isEmpty())
    }

    @Test
    fun `parseDefinitions extracts the example for the sense that has one`() {
        val definitions = WiktionaryWikitextParser.parseDefinitions(sampleWikitext)

        assertTrue(definitions[0].example?.contains("había atravesado la casa") == true)
        assertNull(definitions[1].example)
    }

    @Test
    fun `parseDefinitions strips wiki markup from definition text`() {
        val definitions = WiktionaryWikitextParser.parseDefinitions(sampleWikitext)

        assertTrue(!definitions[0].text.contains("[["))
        assertTrue(!definitions[0].text.contains("''"))
    }

    @Test
    fun `parseDefinitions returns empty list when no numbered senses are present`() {
        val definitions = WiktionaryWikitextParser.parseDefinitions("just some prose, no markup")

        assertTrue(definitions.isEmpty())
    }

    @Test
    fun `parsePronunciation extracts the IPA value when present`() {
        val wikitext = "{{pron-graf|fone1=ˈpe.ro|1audio1=example.wav}}"

        assertEquals("ˈpe.ro", WiktionaryWikitextParser.parsePronunciation(wikitext))
    }

    @Test
    fun `parsePronunciation returns null when no fone parameter is present`() {
        val wikitext = "{{pron-graf|1audio1=example.wav}}"

        assertNull(WiktionaryWikitextParser.parsePronunciation(wikitext))
    }
}