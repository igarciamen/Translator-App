package com.aitranslator.app.ui.liveocr

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextSimilarityTest {

    @Test
    fun `identical strings are similar`() {
        assertTrue(TextSimilarity.isSimilar("Hola mundo", "Hola mundo"))
    }

    @Test
    fun `a single accented character misread is still similar`() {
        assertTrue(TextSimilarity.isSimilar("José Carlos Bermejo", "Josá Carlos Bermejo"))
    }

    @Test
    fun `a couple of misread letters across a longer phrase are still similar`() {
        assertTrue(TextSimilarity.isSimilar("José Carlos Bermejo", "Joso Carlas bermeo"))
    }

    @Test
    fun `completely different short words are not similar`() {
        assertFalse(TextSimilarity.isSimilar("Hola", "Adiós"))
    }

    @Test
    fun `completely different phrases are not similar`() {
        assertFalse(TextSimilarity.isSimilar("José Carlos Bermejo", "Precio: 25 euros"))
    }
}