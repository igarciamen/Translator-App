package com.aitranslator.app.domain.ocr

import android.net.Uri

interface OcrRepository {
    suspend fun extractText(imageUri: Uri): Result<String>
}