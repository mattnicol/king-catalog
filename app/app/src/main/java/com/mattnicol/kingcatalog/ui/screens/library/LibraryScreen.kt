package com.mattnicol.kingcatalog.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Badge
import androidx.compose.material3.IconButton
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mattnicol.kingcatalog.data.model.Book
import com.mattnicol.kingcatalog.ui.components.BookActionSheet
import com.mattnicol.kingcatalog.ui.components.BookCard
import com.mattnicol.kingcatalog.ui.components.FilterSheet
import com.mattnicol.kingcatalog.ui.components.SortSheet
import com.mattnicol.kingcatalog.ui.screens.books.FilterState
import com.mattnicol.kingcatalog.ui.screens.books.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(vm: LibraryViewModel = viewModel(), onBookClick: (Int) -> Unit = {}) {
    val booksByAuthor by vm.ownedBooksByAuthor.collectAsState()
    val storyTypes by vm.storyTypes.collectAsState()
    val decades by vm.decades.collectAsState()
    val genres by vm.genres.collectAsState()
    val keywords by vm.keywords.collectAsState()
    val query by vm.searchQuery.collectAsState()
    val sort by vm.sortOrder.collectAsState()
    val filter by vm.filterState.collectAsState()

    val expandedState = remember { mutableStateMapOf<String, Boolean>() }
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val activeFilterCount = filter.storyTypes.size + filter.genres.size + filter.keywords.size +
        filter.decades.size + (if (filter.bachman != null) 1 else 0) +
        (if (filter.isRead != null) 1 else 0) + (if (filter.inLibrary != null) 1 else 0) +
        (if (filter.bindingFilter != null) 1 else 0)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            OutlinedTextField(
                value = query,
                onValueChange = vm::onQueryChange,
                placeholder = { Text("Search My Library…") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { vm.onQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = false,
                    onClick = { showSortSheet = true },
                    label = {
                        Text(
                            "Sort: " + when (sort) {
                                SortOrder.RELEASE_DATE -> "Date"
                                SortOrder.WORD_COUNT -> "Words"
                                SortOrder.AUDIBLE_LENGTH -> "Audible"
                                SortOrder.GOODREADS_RATING -> "Goodreads"
                            }
                        )
                    },
                    leadingIcon = { Icon(Icons.Filled.Sort, null) },
                )

                BadgedBox(
                    badge = {
                        if (activeFilterCount > 0) {
                            Badge { Text("$activeFilterCount") }
                        }
                    }
                ) {
                    FilterChip(
                        selected = activeFilterCount > 0,
                        onClick = { showFilterSheet = true },
                        label = { Text("Filter") },
                        leadingIcon = { Icon(Icons.Filled.FilterList, null) },
                    )
                }
            }

            if (booksByAuthor.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (query.isBlank() && activeFilterCount == 0)
                            "Your library is empty.\n\nTap any book to open it, then add it to My Library."
                        else
                            "No books match your search or filters.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp),
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    booksByAuthor.forEach { (author, books) ->
                        val isExpanded = expandedState.getOrDefault(author, true)

                        item(key = "header_$author") {
                            TextButton(
                                onClick = { expandedState[author] = !isExpanded },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            ) {
                                Text(
                                    text = author,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f),
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                )
                            }
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                thickness = 1.dp,
                            )
                        }

                        if (isExpanded) {
                            items(books, key = { it.id }) { book ->
                                BookCard(
                                    book = book,
                                    onClick = { onBookClick(book.id) },
                                    onLongClick = { selectedBook = book },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showSortSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSortSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                SortSheet(
                    current = sort,
                    onSelect = { vm.onSortChange(it); showSortSheet = false },
                )
            }
        }

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                FilterSheet(
                    filter = filter,
                    storyTypes = storyTypes,
                    genres = genres,
                    keywords = keywords,
                    decades = decades,
                    onFilterChange = vm::onFilterChange,
                    onClearAll = { vm.onFilterChange(FilterState()); showFilterSheet = false },
                    onDone = { showFilterSheet = false },
                    showBindingFilter = true,
                )
            }
        }

        selectedBook?.let { book ->
            ModalBottomSheet(
                onDismissRequest = { selectedBook = null },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                BookActionSheet(
                    book = book,
                    onDismiss = { selectedBook = null },
                    onAddToLibrary = { binding ->
                        vm.setOwnedWithBinding(book, binding)
                        selectedBook = null
                    },
                    onReadingNow = { vm.setReadingNow(book, !book.isReadingNow); selectedBook = null },
                    onMarkRead = { vm.setRead(book, !book.isRead); selectedBook = null },
                    onToggleReadingList = { vm.setOnReadingList(book, !book.isOnReadingList); selectedBook = null },
                    onRemoveFromLibrary = { vm.setOwned(book, false); selectedBook = null },
                )
            }
        }
    }
}
