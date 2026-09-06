package com.aitranslator.app.data.phrases

import com.aitranslator.app.domain.phrases.CustomPhraseRepository
import com.aitranslator.app.domain.phrases.Phrase
import com.aitranslator.app.domain.phrases.PhraseCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * User-created phrases stored in Room, layered on top of the fixed
 * PhraseCatalog content. Custom phrase ids are prefixed with "custom_"
 * (wrapping the Room-generated Long id) so the UI layer can treat every
 * phrase — built-in or custom — as a single Phrase with a plain String
 * id, only needing to check the prefix to decide whether a delete button
 * should be shown.
 */
class CustomPhraseRepositoryImpl @Inject constructor(
    private val dao: CustomPhraseDao
) : CustomPhraseRepository {

    override fun observeByCategory(category: PhraseCategory): Flow<List<Phrase>> {
        return dao.observeByCategory(category.name).map { entities ->
            entities.map { entity ->
                Phrase(
                    id = "custom_${entity.id}",
                    category = category,
                    englishText = entity.englishText
                )
            }
        }
    }

    override suspend fun addPhrase(category: PhraseCategory, englishText: String) {
        dao.insert(CustomPhraseEntity(categoryName = category.name, englishText = englishText))
    }

    override suspend fun deletePhrase(phraseId: String) {
        val rawId = phraseId.removePrefix("custom_").toLongOrNull() ?: return
        dao.deleteById(rawId)
    }
}