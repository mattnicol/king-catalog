package com.mattnicol.kingcatalog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mattnicol.kingcatalog.data.model.Book
import com.mattnicol.kingcatalog.ui.screens.books.FilterState
import com.mattnicol.kingcatalog.ui.screens.books.SortOrder

fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSheet(current: SortOrder, onSelect: (SortOrder) -> Unit) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    filter: FilterState,
    storyTypes: List<String>,
    genres: List<String>,
    keywords: List<String>,
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

        FilterSection(label = "Keywords") {
            ToggleChipRow(
                allLabel = "All keywords",
                selected = filter.keywords,
                options = keywords.take(30),
                label = { it.replace('-', ' ') },
                onToggle = { k -> onFilterChange(filter.copy(keywords = filter.keywords.toggle(k))) },
                onClear = { onFilterChange(filter.copy(keywords = emptySet())) },
            )
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        FilterSection(label = "Author") {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filter.bachman == null,
                    onClick = { onFilterChange(filter.copy(bachman = null)) },
                    label = { Text("All") },
                )
                FilterChip(
                    selected = filter.bachman == false,
                    onClick = { onFilterChange(filter.copy(bachman = if (filter.bachman == false) null else false)) },
                    label = { Text("King") },
                )
                FilterChip(
                    selected = filter.bachman == true,
                    onClick = { onFilterChange(filter.copy(bachman = if (filter.bachman == true) null else true)) },
                    label = { Text("Bachman") },
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

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
                    onClick = { onFilterChange(filter.copy(isRead = if (filter.isRead == true) null else true)) },
                    label = { Text("Read") },
                )
                FilterChip(
                    selected = filter.isRead == false,
                    onClick = { onFilterChange(filter.copy(isRead = if (filter.isRead == false) null else false)) },
                    label = { Text("Unread") },
                )
            }
        }
    }
}

@Composable
fun FilterSection(label: String, content: @Composable () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 2.dp),
    )
    content()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ToggleChipRow(
    allLabel: String,
    selected: Set<T>,
    options: List<T>,
    label: (T) -> String,
    onToggle: (T) -> Unit,
    onClear: () -> Unit,
) {
    LazyRow(
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
fun BookActionSheet(
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
