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
                val books = SeedImporter.load(this@KingCatalogApp)
                Log.d(TAG, "Seed loaded ${books.size} titles from assets")
                bookRepository.upsertCatalogData(books)
                Log.d(TAG, "Catalog upsert complete")
                userPreferencesRepository.markCatalogVersion(targetVersion)
                Log.d(TAG, "Catalog version marked as $targetVersion")
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
