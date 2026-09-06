package com.aitranslator.app.data.ocr

import android.net.Uri
import com.aitranslator.app.domain.ocr.OcrRepository

class FakeOcrRepository : OcrRepository {
    var lastUri: Uri? = null
    var result: Result<String> = Result.success("")

    override suspend fun extractText(imageUri: Uri): Result<String> {
        lastUri = imageUri
        return result
    }
}