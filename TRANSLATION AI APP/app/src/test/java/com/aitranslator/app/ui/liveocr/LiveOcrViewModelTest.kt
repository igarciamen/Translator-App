package com.aitranslator.app.ui.liveocr

import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.ui.translate.FakeTranslationRepository
import com.aitranslator.app.util.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LiveOcrViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var translationRepository: FakeTranslationRepository
    private lateinit var viewModel: LiveOcrViewModel

    @Before
    fun setUp() {
        translationRepository = FakeTranslationRepository()
        viewModel = LiveOcrViewModel(translationRepository)
    }

    @Test
    fun `onTextBlocksDetected updates the detected blocks and image metadata`() {
        val blocks = listOf(DetectedTextBlock("Hola", BlockBounds(0, 0, 100, 50)))

        viewModel.onTextBlocksDetected(blocks, imageWidth = 1000, imageHeight = 500, rotationDegrees = 90)

        val state = viewModel.uiState.value
        assertEquals(blocks, state.detectedBlocks)
        assertEquals(1000, state.imageWidth)
        assertEquals(500, state.imageHeight)
        assertEquals(90, state.rotationDegrees)
    }

    @Test
    fun `onTextBlocksDetected translates newly seen text`() = runTest {
        translationRepository.result = Result.success("Hello")
        val blocks = listOf(DetectedTextBlock("Hola", BlockBounds(0, 0, 100, 50)))

        viewModel.onTextBlocksDetected(blocks, 1000, 500, 0)
        advanceUntilIdle()

        assertEquals("Hello", viewModel.uiState.value.translations["Hola"])
    }

    @Test
    fun `already translated text is not sent to the repository again`() = runTest {
        translationRepository.result = Result.success("Hello")
        val blocks = listOf(DetectedTextBlock("Hola", BlockBounds(0, 0, 100, 50)))

        viewModel.onTextBlocksDetected(blocks, 1000, 500, 0)
        advanceUntilIdle()
        translationRepository.lastTranslatedText = null

        viewModel.onTextBlocksDetected(blocks, 1000, 500, 0)
        advanceUntilIdle()

        assertEquals(null, translationRepository.lastTranslatedText)
    }

    @Test
    fun `swapping languages clears the translation cache`() = runTest {
        translationRepository.result = Result.success("Hello")
        val blocks = listOf(DetectedTextBlock("Hola", BlockBounds(0, 0, 100, 50)))
        viewModel.onTextBlocksDetected(blocks, 1000, 500, 0)
        advanceUntilIdle()

        viewModel.onSourceLanguageSelected(Language.FRENCH)

        assertTrue(viewModel.uiState.value.translations.isEmpty())
    }

    @Test
    fun `selecting a source language equal to the target swaps them`() {
        viewModel.onSourceLanguageSelected(Language.ENGLISH)

        val state = viewModel.uiState.value
        assertEquals(Language.ENGLISH, state.sourceLanguage)
        assertEquals(Language.SPANISH, state.targetLanguage)
    }

    @Test
    fun `display keeps the first detection stable on minor OCR jitter shortly after`() = runTest {
        translationRepository.result = Result.success("Hello")
        val firstBlocks = listOf(DetectedTextBlock("José Carlos Bermejo", BlockBounds(0, 0, 100, 50)))
        viewModel.onTextBlocksDetected(firstBlocks, 1000, 500, 0)
        advanceUntilIdle()

        val jitteredBlocks = listOf(DetectedTextBlock("Josá Carlos Bermejo", BlockBounds(2, 1, 98, 49)))
        viewModel.onTextBlocksDetected(jitteredBlocks, 1000, 500, 0)
        advanceUntilIdle()

        assertEquals(firstBlocks, viewModel.uiState.value.detectedBlocks)
    }

    @Test
    fun `display updates immediately when detected text is substantially different`() = runTest {
        translationRepository.result = Result.success("Hello")
        val firstBlocks = listOf(DetectedTextBlock("José Carlos Bermejo", BlockBounds(0, 0, 100, 50)))
        viewModel.onTextBlocksDetected(firstBlocks, 1000, 500, 0)
        advanceUntilIdle()

        val newBlocks = listOf(DetectedTextBlock("Precio: 25 euros", BlockBounds(0, 0, 100, 50)))
        viewModel.onTextBlocksDetected(newBlocks, 1000, 500, 0)
        advanceUntilIdle()

        assertEquals(newBlocks, viewModel.uiState.value.detectedBlocks)
    }

    @Test
    fun `translations are still computed for blocks even when the display does not update`() = runTest {
        translationRepository.result = Result.success("Hello")
        val firstBlocks = listOf(DetectedTextBlock("José Carlos Bermejo", BlockBounds(0, 0, 100, 50)))
        viewModel.onTextBlocksDetected(firstBlocks, 1000, 500, 0)
        advanceUntilIdle()

        val jitteredBlocks = listOf(DetectedTextBlock("Josá Carlos Bermejo", BlockBounds(2, 1, 98, 49)))
        viewModel.onTextBlocksDetected(jitteredBlocks, 1000, 500, 0)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.translations.containsKey("Josá Carlos Bermejo"))
    }

    @Test
    fun `onScreenTapped freezes the display`() {
        viewModel.onScreenTapped()
        assertTrue(viewModel.uiState.value.isFrozen)
    }

    @Test
    fun `tapping again unfreezes the display`() {
        viewModel.onScreenTapped()
        viewModel.onScreenTapped()
        assertFalse(viewModel.uiState.value.isFrozen)
    }

    @Test
    fun `detections are ignored while frozen`() = runTest {
        val firstBlocks = listOf(DetectedTextBlock("José Carlos Bermejo", BlockBounds(0, 0, 100, 50)))
        viewModel.onTextBlocksDetected(firstBlocks, 1000, 500, 0)
        advanceUntilIdle()

        viewModel.onScreenTapped()

        val newBlocks = listOf(DetectedTextBlock("Precio: 25 euros", BlockBounds(0, 0, 100, 50)))
        viewModel.onTextBlocksDetected(newBlocks, 1000, 500, 0)
        advanceUntilIdle()

        assertEquals(firstBlocks, viewModel.uiState.value.detectedBlocks)
    }
}