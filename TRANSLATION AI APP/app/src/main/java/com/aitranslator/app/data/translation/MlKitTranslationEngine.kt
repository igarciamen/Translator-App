package com.aitranslator.app.data.translation

import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.domain.translation.TranslationEngine
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import android.util.Log

/**
 * ML Kit-backed engine. A separate Translator client is cached per language
 * pair for translation, but downloaded models are tracked per individual
 * language, matching how ML Kit actually stores them on device.
 */
class MlKitTranslationEngine @Inject constructor() : TranslationEngine {

    private data class LanguagePairKey(val source: String, val target: String)

    private val translators = ConcurrentHashMap<LanguagePairKey, Translator>()
    private val modelManager by lazy { RemoteModelManager.getInstance() }

    override suspend fun translate(
        text: String,
        sourceLanguage: Language,
        targetLanguage: Language
    ): Result<String> {
        Log.d(
            "MlKitTranslate",
            "Translating '$text' from ${sourceLanguage.isoCode} to ${targetLanguage.isoCode}"
        )
        return try {
            val translator = translatorFor(sourceLanguage, targetLanguage)
            val downloadConditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(downloadConditions).await()
            val translated = translator.translate(text).await()
            Log.d("MlKitTranslate", "Result: '$translated'")
            Result.success(translated)
        } catch (error: Exception) {
            Log.e("MlKitTranslate", "Translation failed", error)
            Result.failure(error)
        }
    }

    override suspend fun isModelDownloaded(language: Language): Boolean {
        return try {
            val model = remoteModelFor(language)
            modelManager.isModelDownloaded(model).await()
        } catch (error: Exception) {
            false
        }
    }

    override suspend fun downloadModel(language: Language, requireWifi: Boolean): Result<Unit> {
        return try {
            val model = remoteModelFor(language)
            val conditionsBuilder = DownloadConditions.Builder()
            if (requireWifi) conditionsBuilder.requireWifi()
            modelManager.download(model, conditionsBuilder.build()).await()
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override suspend fun deleteModel(language: Language): Result<Unit> {
        return try {
            val model = remoteModelFor(language)
            modelManager.deleteDownloadedModel(model).await()
            evictCachedTranslatorsFor(language)
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override suspend fun getDownloadedLanguages(): Set<Language> {
        return try {
            val downloadedModels = modelManager
                .getDownloadedModels(TranslateRemoteModel::class.java)
                .await()
            val downloadedCodes = downloadedModels.map { it.language }.toSet()
            Language.supported
                .filter { resolveMlKitCode(it) in downloadedCodes }
                .toSet()
        } catch (error: Exception) {
            emptySet()
        }
    }

    private fun translatorFor(source: Language, target: Language): Translator {
        val sourceCode = resolveMlKitCode(source)
        val targetCode = resolveMlKitCode(target)
        val key = LanguagePairKey(sourceCode, targetCode)

        return translators.getOrPut(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceCode)
                .setTargetLanguage(targetCode)
                .build()
            Translation.getClient(options)
        }
    }

    private fun evictCachedTranslatorsFor(language: Language) {
        val code = resolveMlKitCode(language)
        val keysToRemove = translators.keys.filter { it.source == code || it.target == code }
        keysToRemove.forEach { key ->
            translators.remove(key)?.close()
        }
    }

    private fun remoteModelFor(language: Language): TranslateRemoteModel {
        return TranslateRemoteModel.Builder(resolveMlKitCode(language)).build()
    }

    private fun resolveMlKitCode(language: Language): String {
        return TranslateLanguage.fromLanguageTag(language.isoCode)
            ?: throw IllegalArgumentException("Unsupported language code: ${language.isoCode}")
    }

    override fun close() {
        translators.values.forEach { it.close() }
        translators.clear()
    }
}