package com.aitranslator.app.data.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraCaptureFileNameTest {

    @Test
    fun `generated file name includes the given timestamp`() {
        val name = generateCaptureFileName(timestampMillis = 1234567890L)
        assertEquals("ocr_capture_1234567890.jpg", name)
    }

    @Test
    fun `generated file name ends with jpg extension`() {
        val name = generateCaptureFileName(timestampMillis = 1000L)
        assertTrue(name.endsWith(".jpg"))
    }

    @Test
    fun `different timestamps produce different file names`() {
        val first = generateCaptureFileName(timestampMillis = 1000L)
        val second = generateCaptureFileName(timestampMillis = 2000L)
        assertNotEquals(first, second)
    }
}