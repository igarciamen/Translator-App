package com.aitranslator.app.domain.ocr

import android.net.Uri

interface TextRecognizer {
    suspend fun recognizeText(imageUri: Uri): Result<String>
}