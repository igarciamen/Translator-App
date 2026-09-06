package com.aitranslator.app.ui.liveocr

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FrameThrottleTest {

    @Test
    fun `allows analysis when enough time has passed`() {
        val result = FrameThrottle.shouldAnalyze(
            lastAnalysisTimestampMillis = 1000L,
            currentTimestampMillis = 1600L,
            minIntervalMillis = 500L
        )
        assertTrue(result)
    }

    @Test
    fun `blocks analysis when not enough time has passed`() {
        val result = FrameThrottle.shouldAnalyze(
            lastAnalysisTimestampMillis = 1000L,
            currentTimestampMillis = 1200L,
            minIntervalMillis = 500L
        )
        assertFalse(result)
    }

    @Test
    fun `allows analysis exactly at the interval boundary`() {
        val result = FrameThrottle.shouldAnalyze(
            lastAnalysisTimestampMillis = 1000L,
            currentTimestampMillis = 1500L,
            minIntervalMillis = 500L
        )
        assertTrue(result)
    }

    @Test
    fun `always allows analysis on the very first frame`() {
        val result = FrameThrottle.shouldAnalyze(
            lastAnalysisTimestampMillis = 0L,
            currentTimestampMillis = 1000L,
            minIntervalMillis = 500L
        )
        assertTrue(result)
    }
}