package com.mattnicol.kingcatalog.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mattnicol.kingcatalog.KingCatalogApp
import com.mattnicol.kingcatalog.data.model.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as KingCatalogApp).bookRepository

    val readingNow: Flow<List<Book>> = repo.observeReadingNow()
    val recentlyRead: Flow<List<Book>> = repo.observeRecentlyRead()
    val nextUp: Flow<List<Book>> = repo.observeReadingList()

    fun onReadingNow(book: Book) = viewModelScope.launch { repo.setReadingNow(book, !book.isReadingNow) }
    fun onMarkRead(book: Book) = viewModelScope.launch { repo.setRead(book, !book.isRead) }
    fun onToggleOwned(book: Book) = viewModelScope.launch { repo.setOwned(book, !book.isOwned) }
    fun onToggleReadingList(book: Book) = viewModelScope.launch { repo.setOnReadingList(book, !book.isOnReadingList) }
}
