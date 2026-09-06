package com.aitranslator.app.data.translation

import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.domain.translation.LanguageDetector
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * ML Kit returns "und" when it cannot determine the language, and otherwise
 * a BCP-47 code. This class maps that code back to our own Language enum,
 * failing explicitly when the detected language isn't one we support.
 */
class MlKitLanguageDetector @Inject constructor() : LanguageDetector {

    private val identifier: LanguageIdentifier by lazy {
        LanguageIdentification.getClient()
    }

    override suspend fun detectLanguage(text: String): Result<Language> {
        return try {
            val code = identifier.identifyLanguage(text).await()
            when {
                code == "und" -> Result.failure(
                    LanguageDetectionException("Could not determine the language of the text")
                )
                else -> {
                    val language = Language.fromIsoCode(code)
                    if (language != null) {
                        Result.success(language)
                    } else {
                        Result.failure(
                            LanguageDetectionException("Detected language '$code' is not supported")
                        )
                    }
                }
            }
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override fun close() {
        identifier.close()
    }
}