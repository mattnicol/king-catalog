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

    // Main browseable list: collection parents + non-collection items (hide orphan collection_pieces)
    @Query("""
        SELECT * FROM books
        WHERE author = :author
          AND (is_collection_parent = 1 OR collection_id IS NULL)
        ORDER BY year ASC
    """)
    fun observeBrowseable(author: String = "Stephen King"): Flow<List<BookEntity>>

    @Query("""
        SELECT * FROM books
        WHERE author != 'Stephen King'
          AND (is_collection_parent = 1 OR collection_id IS NULL)
        ORDER BY year ASC
    """)
    fun observeOtherAuthors(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE is_owned = 1 AND author = :author ORDER BY title ASC")
    fun observeOwned(author: String = "Stephen King"): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE is_reading_now = 1 ORDER BY last_status_changed DESC")
    fun observeReadingNow(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE is_read = 1 ORDER BY last_status_changed DESC LIMIT 10")
    fun observeRecentlyRead(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE is_on_reading_list = 1 ORDER BY last_status_changed ASC")
    fun observeReadingList(): Flow<List<BookEntity>>

    @Query("SELECT COUNT(*) FROM books")
    suspend fun count(): Int

    @Query("SELECT DISTINCT story_type FROM books WHERE author = 'Stephen King' ORDER BY story_type ASC")
    fun observeStoryTypes(): Flow<List<String>>

    @Query("SELECT DISTINCT decade FROM books WHERE author = 'Stephen King' AND decade IS NOT NULL ORDER BY decade ASC")
    fun observeDecades(): Flow<List<Int>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun observeById(id: Int): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id IN (:ids) ORDER BY year ASC, title ASC")
    fun observeByIds(ids: List<Int>): Flow<List<BookEntity>>
}
