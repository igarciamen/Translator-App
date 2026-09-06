package com.aitranslator.app.ui.liveocr

object FrameThrottle {
    fun shouldAnalyze(
        lastAnalysisTimestampMillis: Long,
        currentTimestampMillis: Long,
        minIntervalMillis: Long
    ): Boolean {
        return currentTimestampMillis - lastAnalysisTimestampMillis >= minIntervalMillis
    }
}