package com.aitranslator.app.data.ocr

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

interface CameraCaptureUriProvider {
    fun createImageUri(): Uri
}

/**
 * Generates a content:// Uri (backed by FileProvider) pointing at a fresh
 * file in the cache directory, ready to receive a full-size photo from the
 * system camera app via ActivityResultContracts.TakePicture().
 */
class DefaultCameraCaptureUriProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : CameraCaptureUriProvider {

    override fun createImageUri(): Uri {
        val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
        val photoFile = File(imagesDir, generateCaptureFileName())
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }
}

/**
 * Pure function extracted for testability: builds a unique file name for
 * a camera capture based on a timestamp.
 */
fun generateCaptureFileName(timestampMillis: Long = System.currentTimeMillis()): String {
    return "ocr_capture_$timestampMillis.jpg"
}