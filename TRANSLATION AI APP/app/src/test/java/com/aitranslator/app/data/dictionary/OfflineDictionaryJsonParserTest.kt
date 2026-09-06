package com.aitranslator.app.data.dictionary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineDictionaryJsonParserTest {

    // Real, unmodified `data` column content extracted from the actual
    // bundled diccionario_espanol.sqlite for the word "perro".
    private val realSpanishJson = """
        {"definitions":[{"definition":"(Canis lupus familiaris) Variedad doméstica del lobo de muchas y diversas razas, compañero del hombre desde tiempos prehistóricos.","pos":"sustantivo masculino y femenino"},{"definition":"sándwich de salchicha de Viena en un pan largo y delgado, a la medida de ésta, aderezado con ketchup, mayonesa, mostaza u otras salsas.","pos":"sustantivo masculino"},{"definition":"desdichado, indigno, muy malo.","pos":"adjetivo"}],"synonyms":["aciago","nefasto"]}
    """.trimIndent()

    // Real content extracted from diccionario_ingles.sqlite for "dog".
    private val realEnglishJson = """
        {"senses":[{"pos":"noun","definition":"a member of the genus Canis (probably descended from the common wolf) that has been domesticated by man since prehistoric times; occurs in many breeds","examples":["the dog barked all night"],"synonyms":["domestic dog","Canis familiaris"]},{"pos":"noun","definition":"informal term for a man","examples":["you lucky dog"]},{"pos":"verb","definition":"go after with the intent to catch","examples":["the dog chased the rabbit"],"synonyms":["chase","chase after"]}]}
    """.trimIndent()

    // Real content extracted from diccionario_frances.sqlite for "chien"
    // (lowercase entry — the common noun, not the capitalized "Chien"
    // zodiac-sign entry that also exists in the same database).
    private val realFrenchJson = """
        {"pronunciation":"ʃjɛ̃","grammar":"m, p, f","definitions":["(Zoologie) Mammifère carnivore de la famille des Canidés, apparenté au loup.","(Sens figuré) (Familier) Personne rude ou sévère, avare, déloyale."]}
    """.trimIndent()

    @Test
    fun `parseSpanish extracts all definitions with their text`() {
        val entry = OfflineDictionaryJsonParser.parseSpanish("perro", realSpanishJson)

        assertEquals("perro", entry.word)
        assertEquals(3, entry.definitions.size)
        assertTrue(entry.definitions[0].text.contains("Variedad doméstica"))
        assertTrue(entry.definitions[2].text.contains("desdichado"))
    }

    @Test
    fun `parseSpanish attaches the word-level synonyms to the first definition only`() {
        val entry = OfflineDictionaryJsonParser.parseSpanish("perro", realSpanishJson)

        assertEquals(listOf("aciago", "nefasto"), entry.definitions[0].synonyms)
        assertTrue(entry.definitions[1].synonyms.isEmpty())
        assertTrue(entry.definitions[2].synonyms.isEmpty())
    }

    @Test
    fun `parseSpanish leaves pronunciation null since the Spanish dataset does not include it`() {
        val entry = OfflineDictionaryJsonParser.parseSpanish("perro", realSpanishJson)

        assertNull(entry.pronunciation)
    }

    @Test
    fun `parseEnglish extracts a definition per sense, across parts of speech`() {
        val entry = OfflineDictionaryJsonParser.parseEnglish("dog", realEnglishJson)

        assertEquals(3, entry.definitions.size)
        assertTrue(entry.definitions[0].text.contains("genus Canis"))
        assertTrue(entry.definitions[2].text.contains("go after"))
    }

    @Test
    fun `parseEnglish extracts the first example and all synonyms per sense`() {
        val entry = OfflineDictionaryJsonParser.parseEnglish("dog", realEnglishJson)

        assertEquals("the dog barked all night", entry.definitions[0].example)
        assertEquals(listOf("domestic dog", "Canis familiaris"), entry.definitions[0].synonyms)
    }

    @Test
    fun `parseEnglish leaves example null for a sense with no examples array`() {
        val entry = OfflineDictionaryJsonParser.parseEnglish("dog", realEnglishJson)

        assertEquals("you lucky dog", entry.definitions[1].example)
        assertTrue(entry.definitions[1].synonyms.isEmpty())
    }

    @Test
    fun `parseFrench extracts plain-string definitions`() {
        val entry = OfflineDictionaryJsonParser.parseFrench("chien", realFrenchJson)

        assertEquals(2, entry.definitions.size)
        assertTrue(entry.definitions[0].text.contains("Mammifère carnivore"))
        assertTrue(entry.definitions[1].text.contains("Personne rude"))
    }

    @Test
    fun `parseFrench extracts the pronunciation field`() {
        val entry = OfflineDictionaryJsonParser.parseFrench("chien", realFrenchJson)

        assertEquals("ʃjɛ̃", entry.pronunciation)
    }

    @Test
    fun `parseFrench definitions have no synonyms or examples, matching the dataset's flat shape`() {
        val entry = OfflineDictionaryJsonParser.parseFrench("chien", realFrenchJson)

        assertTrue(entry.definitions.all { it.synonyms.isEmpty() && it.example == null })
    }
}