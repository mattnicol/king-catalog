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
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class KingCatalogDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

    companion object {
        @Volatile private var INSTANCE: KingCatalogDatabase? = null

        fun getInstance(context: Context): KingCatalogDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KingCatalogDatabase::class.java,
                    "king_catalog.db",
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
