package com.mattnicol.kingcatalog.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        // v3: includes Malerman, Hill, Hendrix in addition to Stephen King
        val SEED_V3_IMPORTED = booleanPreferencesKey("seed_v3_imported")
    }

    val seedImported: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.SEED_V3_IMPORTED] ?: false }

    suspend fun markSeedImported() {
        context.dataStore.edit { it[Keys.SEED_V3_IMPORTED] = true }
    }
}
