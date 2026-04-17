package com.mattnicol.kingcatalog.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mattnicol.kingcatalog.data.db.converter.Converters
import com.mattnicol.kingcatalog.data.db.dao.BookDao
import com.mattnicol.kingcatalog.data.db.entity.BookEntity

@Database(
    entities = [BookEntity::class],
    version = 5,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class KingCatalogDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

    companion object {
        const val DB_VERSION = 5
        // Bump this whenever king_catalog.json catalog data changes (preserves user fields).
        // Bumped to 9: seed replaced with all_catalog.json (merged, includes non-King authors).
        const val CATALOG_VERSION = 9

        @Volatile private var INSTANCE: KingCatalogDatabase? = null

        fun getInstance(context: Context): KingCatalogDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KingCatalogDatabase::class.java,
                    "king_catalog.db",
                    // IMPORTANT: Do NOT use fallbackToDestructiveMigration() — it wipes user state
                    // (owned/read/reading-list flags) on any schema version bump.
                    // Add explicit migrations for each DB_VERSION increment instead.
                ).build().also { INSTANCE = it }
            }
    }
}
