package com.aitranslator.app.data.ocr

import android.content.Context
import android.net.Uri
import com.aitranslator.app.domain.ocr.TextRecognizer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Uses ML Kit's Latin-script text recognizer. Reliable for
 * Spanish/English/French/German/Italian/Portuguese/Catalan and other
 * Latin-alphabet languages; not suitable for Hindi, Bengali, Tamil, Thai
 * or Chinese, which need separate script-specific recognizers.
 */
class MlKitTextRecognizer @Inject constructor(
    @ApplicationContext private val context: Context
) : TextRecognizer {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    override suspend fun recognizeText(imageUri: Uri): Result<String> {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val result = recognizer.process(image).await()
            val text = result.text

            if (text.isBlank()) {
                Result.failure(OcrException("No text was found in the image"))
            } else {
                Result.success(text)
            }
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}