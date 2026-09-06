package com.aitranslator.app.data.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.aitranslator.app.domain.speech.SpeechToTextEngine
import com.aitranslator.app.domain.translation.Language
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Wraps Android's built-in SpeechRecognizer. This relies on the system's
 * default recognition service (typically Google's, requiring network
 * connectivity) — the fully offline path is a separate block (4.3) built
 * on Vosk. A new SpeechRecognizer is created per listen() call and
 * destroyed afterwards, as recommended by the SDK rather than kept alive
 * as a long-lived singleton.
 */
class AndroidSpeechToTextEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : SpeechToTextEngine {

    override suspend fun listen(language: Language): Result<String> {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return Result.failure(
                SpeechToTextException("Speech recognition is not available on this device")
            )
        }

        return suspendCancellableCoroutine { continuation ->
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)

            val listener = object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val matches = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val bestMatch = matches?.firstOrNull()

                    if (continuation.isActive) {
                        if (bestMatch.isNullOrBlank()) {
                            continuation.resume(
                                Result.failure(SpeechToTextException("Could not understand the audio"))
                            )
                        } else {
                            continuation.resume(Result.success(bestMatch))
                        }
                    }
                    recognizer.destroy()
                }

                override fun onError(error: Int) {
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(SpeechToTextException(mapErrorMessage(error))))
                    }
                    recognizer.destroy()
                }

                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            }

            recognizer.setRecognitionListener(listener)

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale(language.isoCode).toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            recognizer.startListening(intent)

            continuation.invokeOnCancellation {
                recognizer.stopListening()
                recognizer.destroy()
            }
        }
    }

    private fun mapErrorMessage(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                "A network connection is needed for speech recognition"
            SpeechRecognizer.ERROR_NO_MATCH ->
                "Could not understand the audio"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                "No speech was detected"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                "Microphone permission is required"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                "Speech recognizer is busy, try again"
            else ->
                "Speech recognition failed"
        }
    }
}