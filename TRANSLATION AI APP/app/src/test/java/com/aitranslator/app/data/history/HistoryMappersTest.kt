package com.aitranslator.app.data.history

import com.aitranslator.app.domain.translation.Language
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HistoryMappersTest {

    @Test
    fun `toDomain maps a valid entity correctly`() {
        val entity = TranslationHistoryEntity(
            id = 1L,
            sourceText = "Hola",
            translatedText = "Hello",
            sourceLanguageCode = "es",
            targetLanguageCode = "en",
            timestampMillis = 1000L
        )

        val domain = entity.toDomain()

        assertEquals(1L, domain?.id)
        assertEquals(Language.SPANISH, domain?.sourceLanguage)
        assertEquals(Language.ENGLISH, domain?.targetLanguage)
    }

    @Test
    fun `toDomain returns null when the language code is not supported`() {
        val entity = TranslationHistoryEntity(
            id = 1L,
            sourceText = "Hola",
            translatedText = "Hello",
            sourceLanguageCode = "xx",
            targetLanguageCode = "en",
            timestampMillis = 1000L
        )

        assertNull(entity.toDomain())
    }
}