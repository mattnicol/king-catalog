package com.mattnicol.kingcatalog.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mattnicol.kingcatalog.data.db.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class BookDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertAll(books: List<BookEntity>)

    @Update
    abstract suspend fun update(book: BookEntity)

    @Query("SELECT * FROM books WHERE id = :id")
    abstract suspend fun getById(id: Int): BookEntity?

    data class UpsertResult(
        val totalProcessed: Int,
        val inserted: Int,
        val updated: Int,
        val userStatePreserved: Int,
    )

    /**
     * Inserts new books and updates catalog fields on existing ones,
     * preserving all user fields (owned/read/reading-list/binding/notes/imdb_url).
     */
    @Transaction
    open suspend fun upsertCatalogData(books: List<BookEntity>): UpsertResult {
        var inserted = 0
        var updated = 0
        var userStatePreserved = 0
        for (book in books) {
            val existing = getById(book.id)
            if (existing != null) {
                val hadUserState = existing.isOwned || existing.isRead ||
                    existing.isReadingNow || existing.isOnReadingList ||
                    existing.notes != null || existing.bindingOwned != null
                update(book.copy(
                    isOwned = existing.isOwned,
                    isRead = existing.isRead,
                    isReadingNow = existing.isReadingNow,
                    isOnReadingList = existing.isOnReadingList,
                    lastStatusChanged = existing.lastStatusChanged,
                    imdbUrl = existing.imdbUrl,
                    notes = existing.notes,
                    bindingOwned = existing.bindingOwned,
                    bindingWanted = existing.bindingWanted,
                ))
                updated++
                if (hadUserState) userStatePreserved++
            } else {
                insertAll(listOf(book))
                inserted++
            }
        }
        return UpsertResult(
            totalProcessed = books.size,
            inserted = inserted,
            updated = updated,
            userStatePreserved = userStatePreserved,
        )
    }

    @Query("SELECT * FROM books ORDER BY year ASC")
    abstract fun observeAll(): Flow<List<BookEntity>>

    // Main browseable list: Stephen King + Richard Bachman, collection parents + non-collection items
    @Query("""
        SELECT * FROM books
        WHERE author IN ('Stephen King', 'Richard Bachman')
          AND (is_collection_parent = 1 OR collection_id IS NULL)
        ORDER BY year ASC
    """)
    abstract fun observeBrowseable(): Flow<List<BookEntity>>

    @Query("""
        SELECT * FROM books
        WHERE author NOT IN ('Stephen King', 'Richard Bachman')
          AND (is_collection_parent = 1 OR collection_id IS NULL)
        ORDER BY year ASC
    """)
    abstract fun observeOtherAuthors(): Flow<List<BookEntity>>

    // All owned books across all authors
    @Query("SELECT * FROM books WHERE is_owned = 1 ORDER BY author ASC, title ASC")
    abstract fun observeOwned(): Flow<List<BookEntity>>

    // Browseable list for a specific author (collection parents + non-collection items)
    @Query("""
        SELECT * FROM books
        WHERE author = :author
          AND (is_collection_parent = 1 OR collection_id IS NULL)
        ORDER BY year ASC
    """)
    abstract fun observeByAuthor(author: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE is_reading_now = 1 ORDER BY last_status_changed DESC")
    abstract fun observeReadingNow(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE is_read = 1 ORDER BY last_status_changed DESC LIMIT 10")
    abstract fun observeRecentlyRead(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE is_on_reading_list = 1 ORDER BY last_status_changed ASC")
    abstract fun observeReadingList(): Flow<List<BookEntity>>

    @Query("SELECT COUNT(*) FROM books")
    abstract suspend fun count(): Int

    @Query("SELECT DISTINCT story_type FROM books WHERE author IN ('Stephen King', 'Richard Bachman') ORDER BY story_type ASC")
    abstract fun observeStoryTypes(): Flow<List<String>>

    @Query("SELECT DISTINCT decade FROM books WHERE author IN ('Stephen King', 'Richard Bachman') AND decade IS NOT NULL ORDER BY decade ASC")
    abstract fun observeDecades(): Flow<List<Int>>

    @Query("SELECT * FROM books WHERE id = :id")
    abstract fun observeById(id: Int): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id IN (:ids) ORDER BY year ASC, title ASC")
    abstract fun observeByIds(ids: List<Int>): Flow<List<BookEntity>>

    // Books with any connections data (for Series screen)
    @Query("SELECT * FROM books WHERE connections != '[]' AND connections IS NOT NULL ORDER BY author ASC, year ASC")
    abstract fun observeWithConnections(): Flow<List<BookEntity>>
}
