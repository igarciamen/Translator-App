package com.aitranslator.app.ui.liveocr

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * Analyzes camera frames for text at a throttled interval, discarding any
 * frame that arrives while the previous one is still being processed. Not
 * routed through the domain TextRecognizer interface on purpose: it works
 * directly on CameraX/ML Kit frame types, which are an implementation
 * detail of this screen, not something the rest of the app needs.
 */
class TextBlockAnalyzer(
    private val minIntervalMillis: Long = 700L,
    private val onBlocksDetected: (List<DetectedTextBlock>, Int, Int, Int) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private var lastAnalysisTimestamp = 0L
    private var isBusy = false

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val now = System.currentTimeMillis()
        val mediaImage = imageProxy.image

        val shouldSkip = mediaImage == null ||
                isBusy ||
                !FrameThrottle.shouldAnalyze(lastAnalysisTimestamp, now, minIntervalMillis)

        if (shouldSkip) {
            imageProxy.close()
            return
        }

        isBusy = true
        lastAnalysisTimestamp = now

        val inputImage = InputImage.fromMediaImage(mediaImage!!, imageProxy.imageInfo.rotationDegrees)

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                Log.d(
                    "LiveOcrAnalyzer",
                    "Detected ${visionText.textBlocks.size} blocks: " +
                            visionText.textBlocks.joinToString(" | ") { "'${it.text}' rect=${it.boundingBox}" }
                )

                val blocks = visionText.textBlocks.mapNotNull { block ->
                    block.boundingBox?.let { rect ->
                        DetectedTextBlock(
                            block.text,
                            BlockBounds(rect.left, rect.top, rect.right, rect.bottom)
                        )
                    }
                }
                onBlocksDetected(
                    blocks,
                    inputImage.width,
                    inputImage.height,
                    imageProxy.imageInfo.rotationDegrees
                )
            }
            .addOnCompleteListener {
                isBusy = false
                imageProxy.close()
            }
    }
}