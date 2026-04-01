package com.mattnicol.kingcatalog.ui.screens.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mattnicol.kingcatalog.KingCatalogApp
import com.mattnicol.kingcatalog.data.model.Book
import com.mattnicol.kingcatalog.data.model.matchesFilter
import com.mattnicol.kingcatalog.ui.screens.books.FilterState
import com.mattnicol.kingcatalog.ui.screens.books.SortOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as KingCatalogApp).bookRepository

    val searchQuery = MutableStateFlow("")
    val sortOrder = MutableStateFlow(SortOrder.RELEASE_DATE)
    val filterState = MutableStateFlow(FilterState())

    val storyTypes: StateFlow<List<String>> = repo.observeOwned()
        .map { books -> books.map { it.storyType }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val decades: StateFlow<List<Int>> = repo.observeOwned()
        .map { books -> books.mapNotNull { it.decade }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val genres: StateFlow<List<String>> = repo.observeOwned()
        .map { books -> books.flatMap { it.genres }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val keywords: StateFlow<List<String>> = repo.observeOwned()
        .map { books -> books.flatMap { it.keywords }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val ownedBooks: StateFlow<List<Book>> = combine(
        repo.observeOwned(),
        searchQuery,
        sortOrder,
        filterState,
    ) { all, query, sort, filter ->
        all
            .filter {
                it.matchesFilter(
                    query = query,
                    storyTypeFilter = filter.storyTypes,
                    genreFilter = filter.genres,
                    keywordFilter = filter.keywords,
                    decadeFilter = filter.decades,
                    bachamanFilter = filter.bachman,
                    readFilter = filter.isRead,
                    inLibraryFilter = filter.inLibrary,
                )
            }
            .sortedWith(compareBy(nullsLast()) {
                when (sort) {
                    SortOrder.RELEASE_DATE -> it.year
                    SortOrder.WORD_COUNT -> it.wordCount
                    SortOrder.AUDIBLE_LENGTH -> it.audibleMinutes
                }
            })
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onQueryChange(q: String) { searchQuery.value = q }
    fun onSortChange(s: SortOrder) { sortOrder.value = s }
    fun onFilterChange(f: FilterState) { filterState.value = f }

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
