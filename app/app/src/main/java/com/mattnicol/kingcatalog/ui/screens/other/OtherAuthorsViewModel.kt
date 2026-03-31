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

// Authors displayed as collapsible sections in Other Authors tab (ordered for display)
val OTHER_AUTHORS = listOf("Josh Malerman", "Joe Hill", "Grady Hendrix")

class OtherAuthorsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as KingCatalogApp).bookRepository

    val searchQuery = MutableStateFlow("")

    // Books grouped by author, filtered by search query
    val booksByAuthor: StateFlow<Map<String, List<Book>>> = combine(
        repo.observeOtherAuthors(),
        searchQuery,
    ) { all, query ->
        val filtered = all.filter {
            it.matchesFilter(
                query = query,
                storyTypeFilter = emptySet(),
                genreFilter = emptySet(),
                keywordFilter = emptySet(),
                decadeFilter = emptySet(),
                bachamanFilter = null,
                readFilter = null,
            )
        }
        // Group preserving OTHER_AUTHORS display order; unknown authors appended alphabetically
        val known = OTHER_AUTHORS.associateWith { author ->
            filtered.filter { it.author == author }
        }.filterValues { it.isNotEmpty() }
        val unknown = filtered.filter { it.author !in OTHER_AUTHORS }
            .groupBy { it.author }
            .toSortedMap()
        known + unknown
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    fun onQueryChange(q: String) { searchQuery.value = q }

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
