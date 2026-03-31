package com.mattnicol.kingcatalog.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mattnicol.kingcatalog.data.db.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(books: List<BookEntity>)

    @Update
    suspend fun update(book: BookEntity)

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getById(id: Int): BookEntity?

    @Query("SELECT * FROM books ORDER BY year ASC")
    fun observeAll(): Flow<List<BookEntity>>

    // Main browseable list: Stephen King + Richard Bachman, collection parents + non-collection items
    @Query("""
        SELECT * FROM books
        WHERE author IN ('Stephen King', 'Richard Bachman')
          AND (is_collection_parent = 1 OR collection_id IS NULL)
        ORDER BY year ASC
    """)
    fun observeBrowseable(): Flow<List<BookEntity>>

    @Query("""
        SELECT * FROM books
        WHERE author NOT IN ('Stephen King', 'Richard Bachman')
          AND (is_collection_parent = 1 OR collection_id IS NULL)
        ORDER BY year ASC
    """)
    fun observeOtherAuthors(): Flow<List<BookEntity>>

    // All owned books across all authors
    @Query("SELECT * FROM books WHERE is_owned = 1 ORDER BY author ASC, title ASC")
    fun observeOwned(): Flow<List<BookEntity>>

    // Browseable list for a specific author (collection parents + non-collection items)
    @Query("""
        SELECT * FROM books
        WHERE author = :author
          AND (is_collection_parent = 1 OR collection_id IS NULL)
        ORDER BY year ASC
    """)
    fun observeByAuthor(author: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE is_reading_now = 1 ORDER BY last_status_changed DESC")
    fun observeReadingNow(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE is_read = 1 ORDER BY last_status_changed DESC LIMIT 10")
    fun observeRecentlyRead(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE is_on_reading_list = 1 ORDER BY last_status_changed ASC")
    fun observeReadingList(): Flow<List<BookEntity>>

    @Query("SELECT COUNT(*) FROM books")
    suspend fun count(): Int

    @Query("SELECT DISTINCT story_type FROM books WHERE author IN ('Stephen King', 'Richard Bachman') ORDER BY story_type ASC")
    fun observeStoryTypes(): Flow<List<String>>

    @Query("SELECT DISTINCT decade FROM books WHERE author IN ('Stephen King', 'Richard Bachman') AND decade IS NOT NULL ORDER BY decade ASC")
    fun observeDecades(): Flow<List<Int>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun observeById(id: Int): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id IN (:ids) ORDER BY year ASC, title ASC")
    fun observeByIds(ids: List<Int>): Flow<List<BookEntity>>
}
