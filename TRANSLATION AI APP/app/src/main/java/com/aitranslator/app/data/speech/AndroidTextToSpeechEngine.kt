package com.aitranslator.app.data.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.aitranslator.app.domain.speech.TextToSpeechEngine
import com.aitranslator.app.domain.translation.Language
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Wraps Android's native TextToSpeech engine. Kept alive for the lifetime
 * of the app as a Hilt singleton, matching common practice for this SDK —
 * only stop() is exposed, the underlying engine is never shut down.
 */
@Singleton
class AndroidTextToSpeechEngine @Inject constructor(
    @ApplicationContext context: Context
) : TextToSpeechEngine {

    private val readyDeferred = CompletableDeferred<Boolean>()

    private val tts: TextToSpeech = TextToSpeech(context) { status ->
        readyDeferred.complete(status == TextToSpeech.SUCCESS)
    }

    override suspend fun speak(text: String, language: Language): Result<Unit> {
        val isReady = readyDeferred.await()
        if (!isReady) {
            return Result.failure(TextToSpeechException("Text-to-speech engine failed to initialize"))
        }

        val locale = Locale(language.isoCode)
        val availability = tts.isLanguageAvailable(locale)
        if (availability == TextToSpeech.LANG_MISSING_DATA || availability == TextToSpeech.LANG_NOT_SUPPORTED) {
            return Result.failure(
                TextToSpeechException("This language is not available in the device's text-to-speech engine")
            )
        }
        tts.language = locale

        return suspendCancellableCoroutine { continuation ->
            val utteranceId = UUID.randomUUID().toString()

            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit

                override fun onDone(utteranceId: String?) {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }

                @Deprecated("Deprecated in the Android SDK, but still the callback that fires")
                override fun onError(utteranceId: String?) {
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(TextToSpeechException("Speech playback failed")))
                    }
                }
            })

            val queuedSuccessfully = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            if (queuedSuccessfully != TextToSpeech.SUCCESS && continuation.isActive) {
                continuation.resume(Result.failure(TextToSpeechException("Could not start speech playback")))
            }

            continuation.invokeOnCancellation { tts.stop() }
        }
    }

    override fun stop() {
        tts.stop()
    }
}