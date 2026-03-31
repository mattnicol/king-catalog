package com.mattnicol.kingcatalog.ui.screens.books

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mattnicol.kingcatalog.KingCatalogApp
import com.mattnicol.kingcatalog.data.model.Book
import com.mattnicol.kingcatalog.data.model.matchesFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOrder { RELEASE_DATE, WORD_COUNT, AUDIBLE_LENGTH }

data class FilterState(
    val storyTypes: Set<String> = emptySet(),
    val genres: Set<String> = emptySet(),
    val keywords: Set<String> = emptySet(),
    val decades: Set<Int> = emptySet(),
    val bachman: Boolean? = null,
    val isRead: Boolean? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class BooksViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as KingCatalogApp).bookRepository

    val searchQuery = MutableStateFlow("")
    val sortOrder = MutableStateFlow(SortOrder.RELEASE_DATE)
    val filterState = MutableStateFlow(FilterState())

    val storyTypes: StateFlow<List<String>> = repo.observeStoryTypes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val decades: StateFlow<List<Int>> = repo.observeDecades()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val genres: StateFlow<List<String>> = repo.observeBrowseable()
        .map { books -> books.flatMap { it.genres }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val keywords: StateFlow<List<String>> = repo.observeBrowseable()
        .map { books -> books.flatMap { it.keywords }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val books: StateFlow<List<Book>> = combine(
        repo.observeBrowseable(),
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

    fun onReadingNow(book: Book) = viewModelScope.launch { repo.setReadingNow(book, !book.isReadingNow) }
    fun onMarkRead(book: Book) = viewModelScope.launch { repo.setRead(book, !book.isRead) }
    fun onToggleOwned(book: Book) = viewModelScope.launch { repo.setOwned(book, !book.isOwned) }
    fun onToggleReadingList(book: Book) = viewModelScope.launch { repo.setOnReadingList(book, !book.isOnReadingList) }
}
