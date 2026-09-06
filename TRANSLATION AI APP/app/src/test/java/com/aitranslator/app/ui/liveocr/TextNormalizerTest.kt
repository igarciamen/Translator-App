package com.aitranslator.app.ui.liveocr

import org.junit.Assert.assertEquals
import org.junit.Test

class TextNormalizerTest {

    @Test
    fun `trims leading and trailing whitespace`() {
        assertEquals("Hola", TextNormalizer.normalize("  Hola  "))
    }

    @Test
    fun `collapses internal newlines and multiple spaces into a single space`() {
        assertEquals(
            "CUENTOS QUE SANAN",
            TextNormalizer.normalize("CUENTOS\nQUE\nSANAN")
        )
    }

    @Test
    fun `already normalized text is returned unchanged`() {
        assertEquals("Hola mundo", TextNormalizer.normalize("Hola mundo"))
    }
}