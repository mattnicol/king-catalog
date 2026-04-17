package com.mattnicol.kingcatalog.data.repository

import com.mattnicol.kingcatalog.data.db.dao.BookDao
import com.mattnicol.kingcatalog.data.db.entity.BookEntity
import com.mattnicol.kingcatalog.data.db.entity.toDomain
import com.mattnicol.kingcatalog.data.model.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class BookRepository(private val dao: BookDao) {

    fun observeBrowseable(): Flow<List<Book>> =
        dao.observeBrowseable().map { list -> list.map { it.toDomain() } }

    fun observeOtherAuthors(): Flow<List<Book>> =
        dao.observeOtherAuthors().map { list -> list.map { it.toDomain() } }

    fun observeOwned(): Flow<List<Book>> =
        dao.observeOwned().map { list -> list.map { it.toDomain() } }

    fun observeByAuthor(author: String): Flow<List<Book>> =
        dao.observeByAuthor(author).map { list -> list.map { it.toDomain() } }

    fun observeReadingNow(): Flow<List<Book>> =
        dao.observeReadingNow().map { list -> list.map { it.toDomain() } }

    fun observeRecentlyRead(): Flow<List<Book>> =
        dao.observeRecentlyRead().map { list -> list.map { it.toDomain() } }

    fun observeReadingList(): Flow<List<Book>> =
        dao.observeReadingList().map { list -> list.map { it.toDomain() } }

    fun observeStoryTypes(): Flow<List<String>> = dao.observeStoryTypes()

    fun observeDecades(): Flow<List<Int>> = dao.observeDecades()

    fun observeById(id: Int): Flow<Book?> =
        dao.observeById(id).map { it?.toDomain() }

    fun observeChildren(ids: List<Int>): Flow<List<Book>> =
        if (ids.isEmpty()) flowOf(emptyList())
        else dao.observeByIds(ids).map { list -> list.map { it.toDomain() } }

    fun observeWithConnections(): Flow<List<Book>> =
        dao.observeWithConnections().map { list -> list.map { it.toDomain() } }

    suspend fun count(): Int = dao.count()

    suspend fun insertAll(books: List<BookEntity>) = dao.insertAll(books)

    suspend fun upsertCatalogData(books: List<BookEntity>): BookDao.UpsertResult =
        dao.upsertCatalogData(books)

    suspend fun updateBook(book: Book) = dao.update(
        dao.getById(book.id)!!.copy(
            isOwned = book.isOwned,
            isRead = book.isRead,
            isReadingNow = book.isReadingNow,
            isOnReadingList = book.isOnReadingList,
            lastStatusChanged = book.lastStatusChanged,
            notes = book.notes,
            imdbUrl = book.imdbUrl,
            bindingOwned = book.bindingOwned,
            bindingWanted = book.bindingWanted,
        )
    )

    suspend fun setReadingNow(book: Book, value: Boolean) {
        val entity = dao.getById(book.id) ?: return
        dao.update(entity.copy(
            isReadingNow = value,
            lastStatusChanged = System.currentTimeMillis(),
        ))
    }

    suspend fun setRead(book: Book, value: Boolean) {
        val entity = dao.getById(book.id) ?: return
        dao.update(entity.copy(
            isRead = value,
            isReadingNow = if (value) false else entity.isReadingNow,
            lastStatusChanged = System.currentTimeMillis(),
        ))
    }

    suspend fun setOwned(book: Book, value: Boolean) {
        val entity = dao.getById(book.id) ?: return
        dao.update(entity.copy(isOwned = value))
    }

    suspend fun setOwnedWithBinding(book: Book, binding: String?) {
        val entity = dao.getById(book.id) ?: return
        dao.update(entity.copy(isOwned = true, bindingOwned = binding))
    }

    suspend fun setBinding(book: Book, bindingOwned: String?, bindingWanted: String?) {
        val entity = dao.getById(book.id) ?: return
        dao.update(entity.copy(bindingOwned = bindingOwned, bindingWanted = bindingWanted))
    }

    suspend fun setOnReadingList(book: Book, value: Boolean) {
        val entity = dao.getById(book.id) ?: return
        dao.update(entity.copy(
            isOnReadingList = value,
            lastStatusChanged = System.currentTimeMillis(),
        ))
    }
}
