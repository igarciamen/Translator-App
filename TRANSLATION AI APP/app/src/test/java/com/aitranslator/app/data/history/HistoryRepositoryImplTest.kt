package com.aitranslator.app.data.history

import com.aitranslator.app.domain.translation.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeTranslationHistoryDao : TranslationHistoryDao {
    private var nextId = 1L
    private val entities = mutableListOf<TranslationHistoryEntity>()
    private val flow = MutableStateFlow<List<TranslationHistoryEntity>>(emptyList())

    override suspend fun insert(entity: TranslationHistoryEntity): Long {
        val saved = entity.copy(id = nextId++)
        entities.add(0, saved)
        flow.value = entities.toList()
        return saved.id
    }

    override fun observeAll(): StateFlow<List<TranslationHistoryEntity>> = flow

    override fun observeFavorites() =
        flow.map { list -> list.filter { it.isFavorite } }

    override suspend fun updateFavorite(id: Long, isFavorite: Boolean) {
        val index = entities.indexOfFirst { it.id == id }
        if (index != -1) {
            entities[index] = entities[index].copy(isFavorite = isFavorite)
            flow.value = entities.toList()
        }
    }

    override suspend fun deleteById(id: Long) {
        entities.removeAll { it.id == id }
        flow.value = entities.toList()
    }

    override suspend fun deleteAll() {
        entities.clear()
        flow.value = emptyList()
    }
}

class HistoryRepositoryImplTest {

    @Test
    fun `saveTranslation stores a mapped entity`() = runTest {
        val dao = FakeTranslationHistoryDao()
        val repository = HistoryRepositoryImpl(dao)

        repository.saveTranslation("Hola", "Hello", Language.SPANISH, Language.ENGLISH)

        val entries = repository.observeHistory().first()
        assertEquals(1, entries.size)
        assertEquals("Hola", entries.first().sourceText)
        assertEquals(Language.SPANISH, entries.first().sourceLanguage)
    }

    @Test
    fun `saveTranslation ignores blank text`() = runTest {
        val dao = FakeTranslationHistoryDao()
        val repository = HistoryRepositoryImpl(dao)

        repository.saveTranslation("   ", "Hello", Language.SPANISH, Language.ENGLISH)

        val entries = repository.observeHistory().first()
        assertTrue(entries.isEmpty())
    }

    @Test
    fun `deleteEntry removes only the matching entry`() = runTest {
        val dao = FakeTranslationHistoryDao()
        val repository = HistoryRepositoryImpl(dao)
        repository.saveTranslation("Hola", "Hello", Language.SPANISH, Language.ENGLISH)
        repository.saveTranslation("Bonjour", "Hello", Language.FRENCH, Language.ENGLISH)

        val firstId = repository.observeHistory().first().last().id
        repository.deleteEntry(firstId)

        val remaining = repository.observeHistory().first()
        assertEquals(1, remaining.size)
        assertEquals("Bonjour", remaining.first().sourceText)
    }

    @Test
    fun `clearHistory removes all entries`() = runTest {
        val dao = FakeTranslationHistoryDao()
        val repository = HistoryRepositoryImpl(dao)
        repository.saveTranslation("Hola", "Hello", Language.SPANISH, Language.ENGLISH)

        repository.clearHistory()

        val entries = repository.observeHistory().first()
        assertTrue(entries.isEmpty())
    }

    @Test
    fun `toggleFavorite marks an entry as favorite`() = runTest {
        val dao = FakeTranslationHistoryDao()
        val repository = HistoryRepositoryImpl(dao)
        repository.saveTranslation("Hola", "Hello", Language.SPANISH, Language.ENGLISH)
        val id = repository.observeHistory().first().first().id

        repository.toggleFavorite(id, true)

        val entries = repository.observeHistory().first()
        assertTrue(entries.first().isFavorite)
    }

    @Test
    fun `observeFavorites only emits favorited entries`() = runTest {
        val dao = FakeTranslationHistoryDao()
        val repository = HistoryRepositoryImpl(dao)
        repository.saveTranslation("Hola", "Hello", Language.SPANISH, Language.ENGLISH)
        repository.saveTranslation("Bonjour", "Hello", Language.FRENCH, Language.ENGLISH)
        val allEntries = repository.observeHistory().first()
        repository.toggleFavorite(allEntries.first { it.sourceText == "Bonjour" }.id, true)

        val favorites = repository.observeFavorites().first()

        assertEquals(1, favorites.size)
        assertEquals("Bonjour", favorites.first().sourceText)
    }
}