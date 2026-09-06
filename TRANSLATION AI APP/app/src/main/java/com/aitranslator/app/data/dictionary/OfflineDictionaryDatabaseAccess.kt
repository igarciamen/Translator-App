package com.aitranslator.app.data.dictionary

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
/**
 * Opens a read-only, pre-populated SQLite dictionary bundled as an app
 * asset. Plain android.database.sqlite is used instead of Room, since
 * these are static, externally-produced reference databases (schema:
 * entries(id, word, data)) rather than something our own app manages or
 * migrates — Room's schema-hash validation machinery would add risk
 * without benefit here.
 *
 * Assets can't be queried directly as a database (AssetManager only
 * gives a stream, not random access), so each database is copied once
 * to internal storage on first use and reused afterwards.
 *
 * Lookup strategy: the bundled databases declare `word TEXT COLLATE
 * NOCASE` (so a case-insensitive search works in general), but real
 * data was found to contain case-only-different entries for the same
 * spelling with different meanings (e.g. French "chien" — the common
 * noun for a dog — versus "Chien" — the Chinese zodiac sign). Because
 * uppercase letters sort before lowercase ones in SQLite's default
 * comparison, a plain case-insensitive lookup returns the *capitalized*
 * entry first in thousands of such cases across all three languages
 * (720 in Spanish, 1401 in English, 35939 in French), almost always the
 * less common sense rather than the one the user meant. An exact,
 * case-sensitive match is tried first — matching what the user actually
 * typed — falling back to a case-insensitive search only if that finds
 * nothing.
 */
@Singleton
class OfflineDictionaryDatabaseAccess @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val openDatabases = mutableMapOf<String, SQLiteDatabase>()

    suspend fun findRawEntry(assetFileName: String, word: String): String? =
        withContext(Dispatchers.IO) {
            val database = openDatabases.getOrPut(assetFileName) { openOrCopyDatabase(assetFileName) }

            val exactMatch = queryExact(database, word)
            if (exactMatch != null) return@withContext exactMatch

            queryCaseInsensitive(database, word)
        }

    private fun queryExact(database: SQLiteDatabase, word: String): String? {
        database.rawQuery(
            "SELECT data FROM entries WHERE word = ? COLLATE BINARY LIMIT 1",
            arrayOf(word)
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }

    private fun queryCaseInsensitive(database: SQLiteDatabase, word: String): String? {
        database.rawQuery(
            "SELECT data FROM entries WHERE word = ? LIMIT 1",
            arrayOf(word)
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }

    private fun openOrCopyDatabase(assetFileName: String): SQLiteDatabase {
        val targetFile = File(context.filesDir, "offline-dictionaries/$assetFileName")

        if (!targetFile.exists()) {
            targetFile.parentFile?.mkdirs()
            context.assets.open("dictionaries/$assetFileName").use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
        }

        return SQLiteDatabase.openDatabase(
            targetFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
    }
}