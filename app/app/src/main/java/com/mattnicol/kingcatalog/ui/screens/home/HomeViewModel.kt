package com.mattnicol.kingcatalog.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mattnicol.kingcatalog.KingCatalogApp
import com.mattnicol.kingcatalog.data.model.Book
import kotlinx.coroutines.flow.Flow

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as KingCatalogApp).bookRepository

    val readingNow: Flow<List<Book>> = repo.observeReadingNow()
    val recentlyRead: Flow<List<Book>> = repo.observeRecentlyRead()
    val nextUp: Flow<List<Book>> = repo.observeReadingList()
}
