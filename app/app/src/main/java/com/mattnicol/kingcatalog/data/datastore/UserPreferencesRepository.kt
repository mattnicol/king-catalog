package com.mattnicol.kingcatalog.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val CATALOG_VERSION = intPreferencesKey("catalog_version")
    }

    val catalogVersion: Flow<Int> = context.dataStore.data
        .map { it[Keys.CATALOG_VERSION] ?: 0 }

    suspend fun markCatalogVersion(version: Int) {
        context.dataStore.edit { it[Keys.CATALOG_VERSION] = version }
    }
}
