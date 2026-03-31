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

class OtherAuthorsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as KingCatalogApp).bookRepository

    val searchQuery = MutableStateFlow("")

    val books: StateFlow<List<Book>> = combine(
        repo.observeOtherAuthors(),
        searchQuery,
    ) { all, query ->
        all.filter { it.matchesFilter(query, emptySet(), emptySet(), emptySet(), null) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onQueryChange(q: String) { searchQuery.value = q }
}
