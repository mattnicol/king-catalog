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
        Log.d(TAG, "App created — DB version ${KingCatalogDatabase.DB_VERSION}")
        appScope.launch {
            runCatching { ensureSeedImported() }
                .onFailure { e -> Log.e(TAG, "Seed bootstrap failed", e) }
        }
    }

    private suspend fun ensureSeedImported() {
        val alreadyImported = userPreferencesRepository.seedImported.first()
        val dbCount = runCatching { bookRepository.count() }.getOrDefault(0)
        Log.d(TAG, "Seed check: alreadyImported=$alreadyImported dbCount=$dbCount")

        if (!alreadyImported || dbCount == 0) {
            Log.d(TAG, "Seeding database...")
            runCatching {
                val books = SeedImporter.load(this@KingCatalogApp)
                Log.d(TAG, "Seed loaded ${books.size} titles from assets")
                bookRepository.insertAll(books)
                Log.d(TAG, "Seed inserted ${books.size} titles")
                userPreferencesRepository.markSeedImported()
                Log.d(TAG, "Seed complete")
            }.onFailure { e ->
                Log.e(TAG, "Seed import failed — will retry next launch", e)
                // Do NOT mark as imported so we retry next launch
            }
        }
    }

    companion object {
        private const val TAG = "KingCatalogApp"
    }
}
