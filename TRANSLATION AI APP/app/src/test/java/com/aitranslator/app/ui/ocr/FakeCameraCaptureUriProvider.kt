package com.aitranslator.app.ui.ocr

import android.net.Uri
import com.aitranslator.app.data.ocr.CameraCaptureUriProvider
import io.mockk.mockk

class FakeCameraCaptureUriProvider : CameraCaptureUriProvider {
    var createdUri: Uri = mockk()
    var createImageUriCallCount = 0

    override fun createImageUri(): Uri {
        createImageUriCallCount++
        return createdUri
    }
}