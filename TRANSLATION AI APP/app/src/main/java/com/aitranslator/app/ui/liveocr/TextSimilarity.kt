package com.aitranslator.app.ui.liveocr

import kotlin.math.max
import kotlin.math.min

/**
 * Approximate string comparison tolerant of the kind of small letter-level
 * misreads OCR produces on consecutive frames of the same physical text
 * (a couple of accented/similar-looking characters swapped), while still
 * flagging genuinely different content as different.
 */
object TextSimilarity {

    fun isSimilar(a: String, b: String): Boolean {
        if (a.isEmpty() || b.isEmpty()) return a == b

        val distance = levenshteinDistance(a.lowercase(), b.lowercase())
        val longerLength = max(a.length, b.length)

        // Allow up to 25% of characters to differ (minimum tolerance of 2
        // edits) before treating the strings as different content.
        val allowedDistance = max(2, (longerLength * 0.25).toInt())

        return distance <= allowedDistance
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        val previousRow = IntArray(b.length + 1) { it }
        val currentRow = IntArray(b.length + 1)

        for (i in 1..a.length) {
            currentRow[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                currentRow[j] = min(
                    min(currentRow[j - 1] + 1, previousRow[j] + 1),
                    previousRow[j - 1] + cost
                )
            }
            for (j in 0..b.length) {
                previousRow[j] = currentRow[j]
            }
        }

        return previousRow[b.length]
    }
}