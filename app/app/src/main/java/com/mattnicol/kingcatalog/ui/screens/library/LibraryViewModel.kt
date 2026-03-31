package com.mattnicol.kingcatalog.ui.screens.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mattnicol.kingcatalog.KingCatalogApp
import com.mattnicol.kingcatalog.data.model.Book
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as KingCatalogApp).bookRepository

    val ownedBooks: StateFlow<List<Book>> = repo.observeOwned()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setReadingNow(book: Book, value: Boolean) = viewModelScope.launch {
        repo.setReadingNow(book, value)
    }

    fun setRead(book: Book, value: Boolean) = viewModelScope.launch {
        repo.setRead(book, value)
    }

    fun setOwned(book: Book, value: Boolean) = viewModelScope.launch {
        repo.setOwned(book, value)
    }

    fun setOnReadingList(book: Book, value: Boolean) = viewModelScope.launch {
        repo.setOnReadingList(book, value)
    }
}
