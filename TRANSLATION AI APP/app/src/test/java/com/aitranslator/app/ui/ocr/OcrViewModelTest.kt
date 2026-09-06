package com.aitranslator.app.ui.ocr

import android.net.Uri
import com.aitranslator.app.data.ocr.FakeOcrRepository
import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.ui.translate.FakeTranslationRepository
import com.aitranslator.app.util.MainDispatcherRule
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OcrViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var ocrRepository: FakeOcrRepository
    private lateinit var translationRepository: FakeTranslationRepository
    private lateinit var cameraCaptureUriProvider: FakeCameraCaptureUriProvider
    private lateinit var viewModel: OcrViewModel

    @Before
    fun setUp() {
        ocrRepository = FakeOcrRepository()
        translationRepository = FakeTranslationRepository()
        cameraCaptureUriProvider = FakeCameraCaptureUriProvider()
        viewModel = OcrViewModel(ocrRepository, translationRepository, cameraCaptureUriProvider)
    }

    @Test
    fun `initial state has no image and default languages`() {
        val state = viewModel.uiState.value
        assertNull(state.imageUri)
        assertEquals("", state.extractedText)
        assertEquals(Language.SPANISH, state.sourceLanguage)
        assertEquals(Language.ENGLISH, state.targetLanguage)
    }

    @Test
    fun `onImageSelected extracts text and updates the state`() = runTest {
        val uri = mockk<Uri>()
        ocrRepository.result = Result.success("Hola mundo")

        viewModel.onImageSelected(uri)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(uri, state.imageUri)
        assertEquals("Hola mundo", state.extractedText)
        assertFalse(state.isExtracting)
        assertNull(state.extractionError)
    }

    @Test
    fun `onImageSelected surfaces an extraction failure`() = runTest {
        val uri = mockk<Uri>()
        ocrRepository.result = Result.failure(RuntimeException("No text was found in the image"))

        viewModel.onImageSelected(uri)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("No text was found in the image", state.extractionError)
        assertTrue(state.extractedText.isEmpty())
    }

    @Test
    fun `selecting a new image clears the previous extracted and translated text`() = runTest {
        val firstUri = mockk<Uri>()
        ocrRepository.result = Result.success("Primer texto")
        viewModel.onImageSelected(firstUri)
        advanceUntilIdle()

        val secondUri = mockk<Uri>()
        ocrRepository.result = Result.success("Segundo texto")
        viewModel.onImageSelected(secondUri)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Segundo texto", state.extractedText)
        assertEquals("", state.translatedText)
    }

    @Test
    fun `selecting a source language equal to the target swaps them`() {
        viewModel.onSourceLanguageSelected(Language.ENGLISH)

        val state = viewModel.uiState.value
        assertEquals(Language.ENGLISH, state.sourceLanguage)
        assertEquals(Language.SPANISH, state.targetLanguage)
    }

    @Test
    fun `translateExtractedText does nothing when there is no extracted text`() = runTest {
        viewModel.translateExtractedText()
        advanceUntilIdle()

        assertNull(translationRepository.lastTranslatedText)
    }

    @Test
    fun `translateExtractedText translates the extracted text with the selected languages`() = runTest {
        val uri = mockk<Uri>()
        ocrRepository.result = Result.success("Hola mundo")
        translationRepository.result = Result.success("Hello world")
        viewModel.onImageSelected(uri)
        advanceUntilIdle()

        viewModel.translateExtractedText()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Hello world", state.translatedText)
        assertEquals("Hola mundo", translationRepository.lastTranslatedText)
        assertEquals(Language.SPANISH, translationRepository.lastSourceLanguage)
        assertEquals(Language.ENGLISH, translationRepository.lastTargetLanguage)
    }

    @Test
    fun `translateExtractedText surfaces a translation failure`() = runTest {
        val uri = mockk<Uri>()
        ocrRepository.result = Result.success("Hola mundo")
        translationRepository.result = Result.failure(RuntimeException("No network"))
        viewModel.onImageSelected(uri)
        advanceUntilIdle()

        viewModel.translateExtractedText()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("No network", state.translationError)
        assertTrue(state.translatedText.isEmpty())
    }

    @Test
    fun `prepareCameraCaptureUri delegates to the provider`() {
        val uri = viewModel.prepareCameraCaptureUri()

        assertEquals(cameraCaptureUriProvider.createdUri, uri)
        assertEquals(1, cameraCaptureUriProvider.createImageUriCallCount)
    }

    @Test
    fun `onCameraPermissionDenied updates the state`() {
        viewModel.onCameraPermissionDenied()
        assertTrue(viewModel.uiState.value.cameraPermissionDenied)
    }

    @Test
    fun `onCameraPermissionGranted clears a previous denial`() {
        viewModel.onCameraPermissionDenied()
        viewModel.onCameraPermissionGranted()
        assertFalse(viewModel.uiState.value.cameraPermissionDenied)
    }
}