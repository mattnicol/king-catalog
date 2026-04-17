package com.mattnicol.kingcatalog

import android.app.Application
import android.util.Log
import com.mattnicol.kingcatalog.data.datastore.UserPreferencesRepository
import com.mattnicol.kingcatalog.data.db.KingCatalogDatabase
import com.mattnicol.kingcatalog.data.repository.BookRepository
import com.mattnicol.kingcatalog.data.seed.SeedImporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class KingCatalogApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { KingCatalogDatabase.getInstance(this) }
    val bookRepository by lazy { BookRepository(database.bookDao()) }
    val userPreferencesRepository by lazy { UserPreferencesRepository(this) }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "App created — DB version ${KingCatalogDatabase.DB_VERSION}, catalog version ${KingCatalogDatabase.CATALOG_VERSION}")
        appScope.launch {
            runCatching { ensureCatalogCurrent() }
                .onFailure { e -> Log.e(TAG, "Catalog sync failed", e) }
        }
    }

    private suspend fun ensureCatalogCurrent() {
        val storedVersion = userPreferencesRepository.catalogVersion.first()
        val targetVersion = KingCatalogDatabase.CATALOG_VERSION
        Log.d(TAG, "Catalog check: storedVersion=$storedVersion targetVersion=$targetVersion")

        if (storedVersion < targetVersion) {
            Log.d(TAG, "Catalog out of date — upserting...")
            runCatching {
                val seedPath = "assets/king_catalog.json"
                Log.i(TAG, "SEED SOURCE: $seedPath")

                val books = SeedImporter.load(this@KingCatalogApp)
                Log.i(TAG, "SEED TOTAL ROWS: ${books.size}")

                val authorCounts = books.groupingBy { it.author }.eachCount()
                authorCounts.entries.sortedByDescending { it.value }.forEach { (author, count) ->
                    Log.i(TAG, "  SEED AUTHOR: $author -> $count titles")
                }
                val otherAuthors = authorCounts.keys.filter { it != "Stephen King" && it != "Richard Bachman" }
                Log.i(TAG, "OTHER AUTHORS PRESENT: ${otherAuthors.isNotEmpty()} — $otherAuthors")

                val result = bookRepository.upsertCatalogData(books)
                Log.i(TAG, "UPSERT RESULT: totalProcessed=${result.totalProcessed} inserted=${result.inserted} updated=${result.updated}")
                Log.i(TAG, "USER STATE PRESERVED: ${result.userStatePreserved} rows had user state (owned/read/list/notes/binding)")

                userPreferencesRepository.markCatalogVersion(targetVersion)
                Log.i(TAG, "Catalog version marked as $targetVersion — import complete")
            }.onFailure { e ->
                Log.e(TAG, "Catalog upsert failed — will retry next launch", e)
                // Do NOT mark version so we retry next launch
            }
        }
    }

    companion object {
        private const val TAG = "KingCatalogApp"
    }
}
