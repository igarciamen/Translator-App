package com.aitranslator.app.util

import kotlin.math.pow

object ColorContrast {

    /**
     * Computes the WCAG contrast ratio between two colors expressed as
     * "#RRGGBB" hex strings. Returns a value between 1.0 (no contrast)
     * and 21.0 (maximum contrast, black on white).
     */
    fun contrastRatio(hexA: String, hexB: String): Double {
        val luminanceA = relativeLuminance(hexA)
        val luminanceB = relativeLuminance(hexB)
        val lighter = maxOf(luminanceA, luminanceB)
        val darker = minOf(luminanceA, luminanceB)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(hex: String): Double {
        val (r, g, b) = parseRgb(hex)
        val rLin = linearize(r)
        val gLin = linearize(g)
        val bLin = linearize(b)
        return 0.2126 * rLin + 0.7152 * gLin + 0.0722 * bLin
    }

    private fun linearize(channel: Double): Double {
        return if (channel <= 0.03928) {
            channel / 12.92
        } else {
            ((channel + 0.055) / 1.055).pow(2.4)
        }
    }

    private fun parseRgb(hex: String): Triple<Double, Double, Double> {
        val clean = hex.removePrefix("#")
        require(clean.length == 6) { "Expected a #RRGGBB hex color, got: $hex" }
        val r = clean.substring(0, 2).toInt(16) / 255.0
        val g = clean.substring(2, 4).toInt(16) / 255.0
        val b = clean.substring(4, 6).toInt(16) / 255.0
        return Triple(r, g, b)
    }
}