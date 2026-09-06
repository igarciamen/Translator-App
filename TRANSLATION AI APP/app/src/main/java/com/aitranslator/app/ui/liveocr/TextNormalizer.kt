package com.aitranslator.app.ui.liveocr

object TextNormalizer {
    /**
     * Collapses whitespace/newline differences between OCR reads of the
     * same physical text, so minor formatting jitter doesn't get treated
     * as a brand-new string for caching/translation purposes.
     */
    fun normalize(text: String): String {
        return text.trim().replace(Regex("\\s+"), " ")
    }
}