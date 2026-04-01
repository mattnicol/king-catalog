package com.mattnicol.kingcatalog.ui.screens.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mattnicol.kingcatalog.data.model.Book
import com.mattnicol.kingcatalog.ui.components.BookActionSheet
import com.mattnicol.kingcatalog.ui.components.FilterSheet
import com.mattnicol.kingcatalog.ui.components.SortSheet
import com.mattnicol.kingcatalog.ui.components.BookCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(vm: BooksViewModel = viewModel(), onBookClick: (Int) -> Unit = {}) {
    val books by vm.books.collectAsState()
    val storyTypes by vm.storyTypes.collectAsState()
    val decades by vm.decades.collectAsState()
    val genres by vm.genres.collectAsState()
    val keywords by vm.keywords.collectAsState()
    val query by vm.searchQuery.collectAsState()
    val sort by vm.sortOrder.collectAsState()
    val filter by vm.filterState.collectAsState()

    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val activeFilterCount = filter.storyTypes.size + filter.genres.size + filter.keywords.size +
        filter.decades.size + (if (filter.bachman != null) 1 else 0) +
        (if (filter.isRead != null) 1 else 0) + (if (filter.inLibrary != null) 1 else 0)

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

            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filter.inLibrary == null,
                    onClick = { vm.onFilterChange(filter.copy(inLibrary = null)) },
                    label = { Text("All") },
                )
                FilterChip(
                    selected = filter.inLibrary == true,
                    onClick = { vm.onFilterChange(filter.copy(inLibrary = if (filter.inLibrary == true) null else true)) },
                    label = { Text("In Library") },
                )
                FilterChip(
                    selected = filter.inLibrary == false,
                    onClick = { vm.onFilterChange(filter.copy(inLibrary = if (filter.inLibrary == false) null else false)) },
                    label = { Text("Not In Library") },
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(books, key = { it.id }) { book ->
                    BookCard(
                        book = book,
                        onClick = { onBookClick(book.id) },
                        onLongClick = { selectedBook = book },
                    )
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
                    onAddToLibrary = { vm.onToggleOwned(book); selectedBook = null },
                    onReadingNow = { vm.onReadingNow(book); selectedBook = null },
                    onMarkRead = { vm.onMarkRead(book); selectedBook = null },
                    onToggleReadingList = { vm.onToggleReadingList(book); selectedBook = null },
                    onRemoveFromLibrary = { vm.onToggleOwned(book); selectedBook = null },
                )
            }
        }
    }
}
