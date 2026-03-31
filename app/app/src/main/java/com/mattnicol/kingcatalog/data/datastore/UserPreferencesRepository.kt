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
        val SEED_IMPORTED = booleanPreferencesKey("seed_imported")
    }

    val seedImported: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.SEED_IMPORTED] ?: false }

    suspend fun markSeedImported() {
        context.dataStore.edit { it[Keys.SEED_IMPORTED] = true }
    }
}
