package com.mattnicol.kingcatalog.ui.screens.books

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mattnicol.kingcatalog.data.model.Book
import com.mattnicol.kingcatalog.ui.components.BookCard

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(vm: BooksViewModel = viewModel()) {
    val books by vm.books.collectAsState()
    val storyTypes by vm.storyTypes.collectAsState()
    val decades by vm.decades.collectAsState()
    val genres by vm.genres.collectAsState()
    val query by vm.searchQuery.collectAsState()
    val sort by vm.sortOrder.collectAsState()
    val filter by vm.filterState.collectAsState()
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    val sheetState = rememberModalBottomSheetState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            OutlinedTextField(
                value = query,
                onValueChange = vm::onQueryChange,
                placeholder = { Text("Search books…") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // Sort chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(SortOrder.entries) { s ->
                    FilterChip(
                        selected = sort == s,
                        onClick = { vm.onSortChange(s) },
                        label = {
                            Text(
                                when (s) {
                                    SortOrder.RELEASE_DATE -> "Date"
                                    SortOrder.WORD_COUNT -> "Words"
                                    SortOrder.AUDIBLE_LENGTH -> "Audible"
                                }
                            )
                        },
                    )
                }
            }

            // Story type filter
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = filter.storyType == null,
                        onClick = { vm.onFilterChange(filter.copy(storyType = null)) },
                        label = { Text("All types") },
                    )
                }
                items(storyTypes) { type ->
                    FilterChip(
                        selected = filter.storyType == type,
                        onClick = {
                            vm.onFilterChange(
                                filter.copy(storyType = if (filter.storyType == type) null else type)
                            )
                        },
                        label = { Text(type.replace('_', ' ').replaceFirstChar { it.uppercase() }) },
                    )
                }
            }

            // Genre filter
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = filter.genre == null,
                        onClick = { vm.onFilterChange(filter.copy(genre = null)) },
                        label = { Text("All genres") },
                    )
                }
                items(genres) { genre ->
                    FilterChip(
                        selected = filter.genre == genre,
                        onClick = {
                            vm.onFilterChange(
                                filter.copy(genre = if (filter.genre == genre) null else genre)
                            )
                        },
                        label = { Text(genre.replaceFirstChar { it.uppercase() }) },
                    )
                }
            }

            // Decade filter
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = filter.decade == null,
                        onClick = { vm.onFilterChange(filter.copy(decade = null)) },
                        label = { Text("All decades") },
                    )
                }
                items(decades) { d ->
                    FilterChip(
                        selected = filter.decade == d,
                        onClick = {
                            vm.onFilterChange(
                                filter.copy(decade = if (filter.decade == d) null else d)
                            )
                        },
                        label = { Text("${d}s") },
                    )
                }
            }

            // Read / Unread filter
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = filter.isRead == null,
                    onClick = { vm.onFilterChange(filter.copy(isRead = null)) },
                    label = { Text("All") },
                )
                FilterChip(
                    selected = filter.isRead == true,
                    onClick = { vm.onFilterChange(filter.copy(isRead = if (filter.isRead == true) null else true)) },
                    label = { Text("Read") },
                )
                FilterChip(
                    selected = filter.isRead == false,
                    onClick = { vm.onFilterChange(filter.copy(isRead = if (filter.isRead == false) null else false)) },
                    label = { Text("Unread") },
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(books, key = { it.id }) { book ->
                    Box(
                        modifier = Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = { selectedBook = book },
                        )
                    ) {
                        BookCard(book = book)
                    }
                }
            }
        }

        selectedBook?.let { book ->
            ModalBottomSheet(
                onDismissRequest = { selectedBook = null },
                sheetState = sheetState,
            ) {
                BookQuickActions(
                    book = book,
                    onDismiss = { selectedBook = null },
                    onReadingNow = { vm.onReadingNow(book); selectedBook = null },
                    onMarkRead = { vm.onMarkRead(book); selectedBook = null },
                    onToggleLibrary = { vm.onToggleOwned(book); selectedBook = null },
                    onToggleReadingList = { vm.onToggleReadingList(book); selectedBook = null },
                )
            }
        }
    }
}

@Composable
private fun BookQuickActions(
    book: Book,
    onDismiss: () -> Unit,
    onReadingNow: () -> Unit,
    onMarkRead: () -> Unit,
    onToggleLibrary: () -> Unit,
    onToggleReadingList: () -> Unit,
) {
    TextButton(onClick = onReadingNow, modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(if (book.isReadingNow) "Stop Reading" else "Reading Now")
    }
    TextButton(onClick = onMarkRead, modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(if (book.isRead) "Mark Unread" else "Mark Read")
    }
    TextButton(onClick = onToggleLibrary, modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(if (book.isOwned) "Remove from Library" else "Add to Library")
    }
    TextButton(onClick = onToggleReadingList, modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(if (book.isOnReadingList) "Remove from Reading List" else "Add to Reading List")
    }
    TextButton(onClick = onDismiss, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Cancel")
    }
}
