package com.mattnicol.kingcatalog.ui.screens.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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

@OptIn(ExperimentalMaterial3Api::class)
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
    var showSortSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val activeFilterCount = filter.storyTypes.size + filter.genres.size + filter.decades.size + if (filter.isRead != null) 1 else 0

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

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(books, key = { it.id }) { book ->
                    BookCard(
                        book = book,
                        onLongClick = { selectedBook = book },
                    )
                }
            }
        }

        // Sort bottom sheet
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

        // Filter bottom sheet
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
                    decades = decades,
                    onFilterChange = vm::onFilterChange,
                    onClearAll = { vm.onFilterChange(FilterState()); showFilterSheet = false },
                    onDone = { showFilterSheet = false },
                )
            }
        }

        // Long-press action sheet
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

@Composable
private fun SortSheet(current: SortOrder, onSelect: (SortOrder) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = "Sort by",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        Divider()
        SortOrder.entries.forEach { s ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = current == s, onClick = { onSelect(s) })
                Text(
                    text = when (s) {
                        SortOrder.RELEASE_DATE -> "Release Date"
                        SortOrder.WORD_COUNT -> "Word Count"
                        SortOrder.AUDIBLE_LENGTH -> "Audible Length"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun FilterSheet(
    filter: FilterState,
    storyTypes: List<String>,
    genres: List<String>,
    decades: List<Int>,
    onFilterChange: (FilterState) -> Unit,
    onClearAll: () -> Unit,
    onDone: () -> Unit,
) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Filter", style = MaterialTheme.typography.titleMedium)
            Row {
                TextButton(onClick = onClearAll) { Text("Clear all") }
                TextButton(onClick = onDone) { Text("Done") }
            }
        }
        Divider()

        // Type
        FilterSection(label = "Type") {
            ToggleChipRow(
                allLabel = "All types",
                selected = filter.storyTypes,
                options = storyTypes,
                label = { it.replace('_', ' ').replaceFirstChar { c -> c.uppercase() } },
                onToggle = { t -> onFilterChange(filter.copy(storyTypes = filter.storyTypes.toggle(t))) },
                onClear = { onFilterChange(filter.copy(storyTypes = emptySet())) },
            )
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        // Genre
        FilterSection(label = "Genre") {
            ToggleChipRow(
                allLabel = "All genres",
                selected = filter.genres,
                options = genres.take(20),
                label = { it.replaceFirstChar { c -> c.uppercase() } },
                onToggle = { g -> onFilterChange(filter.copy(genres = filter.genres.toggle(g))) },
                onClear = { onFilterChange(filter.copy(genres = emptySet())) },
            )
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        // Decade
        FilterSection(label = "Decade") {
            ToggleChipRow(
                allLabel = "All decades",
                selected = filter.decades,
                options = decades,
                label = { "${it}s" },
                onToggle = { d -> onFilterChange(filter.copy(decades = filter.decades.toggle(d))) },
                onClear = { onFilterChange(filter.copy(decades = emptySet())) },
            )
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        // Read status
        FilterSection(label = "Status") {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filter.isRead == null,
                    onClick = { onFilterChange(filter.copy(isRead = null)) },
                    label = { Text("All") },
                )
                FilterChip(
                    selected = filter.isRead == true,
                    onClick = {
                        onFilterChange(filter.copy(isRead = if (filter.isRead == true) null else true))
                    },
                    label = { Text("Read") },
                )
                FilterChip(
                    selected = filter.isRead == false,
                    onClick = {
                        onFilterChange(filter.copy(isRead = if (filter.isRead == false) null else false))
                    },
                    label = { Text("Unread") },
                )
            }
        }
    }
}

@Composable
private fun FilterSection(label: String, content: @Composable () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 2.dp),
    )
    content()
}

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item

@Composable
private fun <T> ToggleChipRow(
    allLabel: String,
    selected: Set<T>,
    options: List<T>,
    label: (T) -> String,
    onToggle: (T) -> Unit,
    onClear: () -> Unit,
) {
    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selected.isEmpty(),
                onClick = onClear,
                label = { Text(allLabel) },
            )
        }
        items(options) { opt ->
            FilterChip(
                selected = opt in selected,
                onClick = { onToggle(opt) },
                label = { Text(label(opt)) },
            )
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun BookActionSheet(
    book: Book,
    onDismiss: () -> Unit,
    onAddToLibrary: () -> Unit,
    onReadingNow: () -> Unit,
    onMarkRead: () -> Unit,
    onToggleReadingList: () -> Unit,
    onRemoveFromLibrary: () -> Unit,
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = book.title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            maxLines = 2,
        )
        Divider()
        if (!book.isOwned) {
            TextButton(
                onClick = onAddToLibrary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) { Text("Add to Library") }
        } else {
            TextButton(
                onClick = onReadingNow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) { Text(if (book.isReadingNow) "Stop Reading" else "Reading Now") }
            TextButton(
                onClick = onMarkRead,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) { Text(if (book.isRead) "Mark Unread" else "Mark Read") }
            TextButton(
                onClick = onToggleReadingList,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) { Text(if (book.isOnReadingList) "Remove from Reading List" else "Add to Reading List") }
            TextButton(
                onClick = onRemoveFromLibrary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) { Text("Remove from Library") }
        }
        TextButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        ) { Text("Cancel") }
    }
}
