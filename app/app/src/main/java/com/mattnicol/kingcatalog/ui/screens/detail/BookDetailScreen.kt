package com.mattnicol.kingcatalog.ui.screens.detail

import android.app.Application
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mattnicol.kingcatalog.KingCatalogApp
import com.mattnicol.kingcatalog.data.model.Book
import com.mattnicol.kingcatalog.ui.components.BookCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookDetailViewModel(application: Application, val bookId: Int) : AndroidViewModel(application) {
    private val repo = (application as KingCatalogApp).bookRepository

    val book: StateFlow<Book?> = repo.observeById(bookId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _children = MutableStateFlow<List<Book>>(emptyList())
    val children: StateFlow<List<Book>> = _children

    init {
        viewModelScope.launch {
            book.collect { b ->
                if (b != null && b.isCollectionParent && b.childIds.isNotEmpty()) {
                    repo.observeChildren(b.childIds).collect { _children.value = it }
                }
            }
        }
    }

    fun onReadingNow(book: Book) = viewModelScope.launch { repo.setReadingNow(book, !book.isReadingNow) }
    fun onMarkRead(book: Book) = viewModelScope.launch { repo.setRead(book, !book.isRead) }
    fun onToggleOwned(book: Book) = viewModelScope.launch { repo.setOwned(book, !book.isOwned) }
    fun onToggleReadingList(book: Book) = viewModelScope.launch { repo.setOnReadingList(book, !book.isOnReadingList) }

    companion object {
        fun factory(app: Application, bookId: Int) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                BookDetailViewModel(app, bookId) as T
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookDetailScreen(bookId: Int, onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as KingCatalogApp
    val vm: BookDetailViewModel = viewModel(factory = BookDetailViewModel.factory(app, bookId))
    val book by vm.book.collectAsState()
    val children by vm.children.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.title ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        book?.let { b ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Cover
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        val imageModel: Any? = when {
                            b.coverLocalPath != null -> Uri.parse("file:///android_asset/covers/${b.coverLocalPath}")
                            b.coverCandidateUrl != null -> b.coverCandidateUrl
                            else -> null
                        }
                        if (imageModel != null) {
                            AsyncImage(
                                model = imageModel,
                                contentDescription = b.title,
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(240.dp),
                                contentScale = ContentScale.Fit,
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(240.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = b.title.take(1),
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }
                }

                // Title + author
                item {
                    Column {
                        Text(b.title, style = MaterialTheme.typography.headlineSmall)
                        val authorLine = if (b.asBachman) "Stephen King writing as Richard Bachman" else "Stephen King"
                        Text(
                            authorLine,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }

                // Meta row: year, decade, type
                item {
                    val parts = buildList {
                        b.year?.let { add(it.toString()) }
                        b.decade?.let { add("${it}s") }
                        add(b.storyType.replace('_', ' ').replaceFirstChar { it.uppercase() })
                    }
                    Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
                }

                // Genres
                if (b.genres.isNotEmpty()) {
                    item {
                        DetailSection("Genre") {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                b.genres.forEach { g ->
                                    AssistChip(onClick = {}, label = { Text(g) })
                                }
                            }
                        }
                    }
                }

                // Keywords
                if (b.keywords.isNotEmpty()) {
                    item {
                        DetailSection("Keywords") {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                b.keywords.forEach { k ->
                                    AssistChip(onClick = {}, label = { Text(k.replace('-', ' ')) })
                                }
                            }
                        }
                    }
                }

                // Word count / Audible
                if (b.wordCount != null || b.audibleMinutes != null) {
                    item {
                        val parts = buildList {
                            b.wordCount?.let { add("~${it / 1000}K words") }
                            b.audibleMinutes?.let { mins ->
                                val h = mins / 60
                                val m = mins % 60
                                add(if (h > 0) "${h}h ${m}m audio" else "${m}m audio")
                            }
                        }
                        Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Adaptations
                if (b.hasAdaptation) {
                    item {
                        DetailSection("Adaptations") {
                            b.adaptations.filter { it.year != null || it.status != null }.forEach { a ->
                                val desc = buildString {
                                    a.year?.let { append(it) }
                                    a.title?.let { append(" – $it") }
                                    a.format?.let { append(" ($it)") }
                                    if (a.status == "in_development") append(" [In development]")
                                }
                                if (desc.isNotBlank()) {
                                    Text(desc, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // Collection membership
                b.collection?.let { c ->
                    item {
                        Text("Collection: $c", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Notes
                b.notes?.let { n ->
                    item {
                        DetailSection("Notes") {
                            Text(n, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Actions
                item { Divider() }
                item {
                    DetailActions(
                        book = b,
                        onAddToLibrary = { vm.onToggleOwned(b) },
                        onReadingNow = { vm.onReadingNow(b) },
                        onMarkRead = { vm.onMarkRead(b) },
                        onToggleReadingList = { vm.onToggleReadingList(b) },
                        onRemoveFromLibrary = { vm.onToggleOwned(b) },
                    )
                }

                // Collection children list
                if (b.isCollectionParent && children.isNotEmpty()) {
                    item {
                        Text(
                            "Contents",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Divider()
                    }
                    items(children, key = { it.id }) { child ->
                        BookCard(book = child)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(label: String, content: @Composable () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp),
    )
    content()
}

@Composable
private fun DetailActions(
    book: Book,
    onAddToLibrary: () -> Unit,
    onReadingNow: () -> Unit,
    onMarkRead: () -> Unit,
    onToggleReadingList: () -> Unit,
    onRemoveFromLibrary: () -> Unit,
) {
    Column {
        if (!book.isOwned) {
            TextButton(
                onClick = onAddToLibrary,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Add to Library") }
        } else {
            TextButton(
                onClick = onReadingNow,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (book.isReadingNow) "Stop Reading" else "Reading Now") }
            TextButton(
                onClick = onMarkRead,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (book.isRead) "Mark Unread" else "Mark Read") }
            TextButton(
                onClick = onToggleReadingList,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (book.isOnReadingList) "Remove from Reading List" else "Add to Reading List") }
            TextButton(
                onClick = onRemoveFromLibrary,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Remove from Library") }
        }
    }
}
