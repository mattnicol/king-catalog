package com.mattnicol.kingcatalog

import android.app.Application
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
        appScope.launch {
            val alreadyImported = userPreferencesRepository.seedImported.first()
            if (!alreadyImported) {
                val books = SeedImporter.load(this@KingCatalogApp)
                bookRepository.insertAll(books)
                userPreferencesRepository.markSeedImported()
            }
        }
    }
}
