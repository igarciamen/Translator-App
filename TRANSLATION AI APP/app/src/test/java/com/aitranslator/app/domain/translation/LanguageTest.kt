package com.aitranslator.app.domain.translation

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageTest {
    // ... el resto del archivo queda igual

    @Test
    fun `supported languages have no duplicate ISO codes`() {
        val codes = Language.supported.map { it.isoCode }
        assertEquals(codes.size, codes.toSet().size)
    }

    @Test
    fun `supported list contains at least Spanish and English`() {
        Assert.assertTrue(Language.SPANISH in Language.supported)
        Assert.assertTrue(Language.ENGLISH in Language.supported)
    }

    @Test
    fun `every language has a two-letter lowercase ISO code`() {
        Language.supported.forEach { language ->
            Assert.assertEquals(2, language.isoCode.length)
            assertEquals(language.isoCode, language.isoCode.lowercase())
        }
    }

    @Test
    fun `supported list now includes the new South and Southeast Asian languages`() {
        val expectedAdditions = listOf(
            Language.HINDI, Language.BENGALI, Language.TAMIL, Language.VIETNAMESE,
            Language.THAI, Language.INDONESIAN, Language.MALAY, Language.TAGALOG
        )
        expectedAdditions.forEach { language ->
            assertTrue("${language.name} should be in the supported list", language in Language.supported)
        }
    }

    @Test
    fun `supported list contains exactly 16 languages`() {
        assertEquals(16, Language.supported.size)
    }

    @Test
    fun `fromIsoCode returns the matching language for a known code`() {
        assertEquals(Language.ENGLISH, Language.fromIsoCode("en"))
        assertEquals(Language.HINDI, Language.fromIsoCode("hi"))
    }

    @Test
    fun `fromIsoCode returns null for an unsupported code`() {
        assertEquals(null, Language.fromIsoCode("sw"))
        assertEquals(null, Language.fromIsoCode("und"))
    }
}