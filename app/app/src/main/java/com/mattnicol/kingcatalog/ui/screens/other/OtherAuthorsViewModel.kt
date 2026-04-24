package com.mattnicol.kingcatalog.ui.screens.other

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mattnicol.kingcatalog.KingCatalogApp
import com.mattnicol.kingcatalog.data.model.Book
import com.mattnicol.kingcatalog.data.model.matchesFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Known other authors (informational; display order is alphabetical)
val OTHER_AUTHORS = listOf("Grady Hendrix", "Joe Hill", "Josh Malerman", "Paul Tremblay")

class OtherAuthorsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as KingCatalogApp).bookRepository

    val searchQuery = MutableStateFlow("")
    val inLibraryFilter = MutableStateFlow<Boolean?>(null)

    // Books grouped by author, filtered by search query and library state
    val booksByAuthor: StateFlow<Map<String, List<Book>>> = combine(
        repo.observeOtherAuthors(),
        searchQuery,
        inLibraryFilter,
    ) { all, query, inLibrary ->
        val filtered = all.filter {
            it.matchesFilter(
                query = query,
                storyTypeFilter = emptySet(),
                genreFilter = emptySet(),
                keywordFilter = emptySet(),
                decadeFilter = emptySet(),
                bachamanFilter = null,
                readFilter = null,
                inLibraryFilter = inLibrary,
            )
        }
        // Group by author, sorted alphabetically
        filtered.groupBy { it.author }.toSortedMap()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    fun onQueryChange(q: String) { searchQuery.value = q }
    fun onInLibraryFilter(v: Boolean?) { inLibraryFilter.value = v }

    fun onReadingNow(book: Book) = viewModelScope.launch {
        repo.setReadingNow(book, !book.isReadingNow)
    }

    fun onMarkRead(book: Book) = viewModelScope.launch {
        repo.setRead(book, !book.isRead)
    }

    fun onToggleOwned(book: Book) = viewModelScope.launch {
        repo.setOwned(book, !book.isOwned)
    }

    fun onToggleReadingList(book: Book) = viewModelScope.launch {
        repo.setOnReadingList(book, !book.isOnReadingList)
    }
}
