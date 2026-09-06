package com.aitranslator.app.data.ocr

import android.net.Uri
import com.aitranslator.app.domain.ocr.TextRecognizer
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeTextRecognizer : TextRecognizer {
    var lastUri: Uri? = null
    var result: Result<String> = Result.success("")

    override suspend fun recognizeText(imageUri: Uri): Result<String> {
        lastUri = imageUri
        return result
    }
}

class OcrRepositoryImplTest {

    @Test
    fun `extractText forwards the uri to the recognizer`() = runTest {
        val recognizer = FakeTextRecognizer()
        recognizer.result = Result.success("Hola mundo")
        val repository = OcrRepositoryImpl(recognizer)
        val uri = mockk<Uri>()

        val result = repository.extractText(uri)

        assertEquals(uri, recognizer.lastUri)
        assertEquals("Hola mundo", result.getOrNull())
    }

    @Test
    fun `extractText propagates a recognizer failure`() = runTest {
        val recognizer = FakeTextRecognizer()
        recognizer.result = Result.failure(OcrException("No text was found in the image"))
        val repository = OcrRepositoryImpl(recognizer)
        val uri = mockk<Uri>()

        val result = repository.extractText(uri)

        assertTrue(result.isFailure)
    }
}