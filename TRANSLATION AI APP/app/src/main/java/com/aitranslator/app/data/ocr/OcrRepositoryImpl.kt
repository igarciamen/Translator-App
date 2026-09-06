package com.aitranslator.app.data.ocr

import android.net.Uri
import com.aitranslator.app.domain.ocr.OcrRepository
import com.aitranslator.app.domain.ocr.TextRecognizer
import javax.inject.Inject

class OcrRepositoryImpl @Inject constructor(
    private val recognizer: TextRecognizer
) : OcrRepository {

    override suspend fun extractText(imageUri: Uri): Result<String> {
        return recognizer.recognizeText(imageUri)
    }
}