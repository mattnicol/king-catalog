package com.mattnicol.kingcatalog.ui.screens.other

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mattnicol.kingcatalog.data.model.Book
import com.mattnicol.kingcatalog.ui.components.BookActionSheet
import com.mattnicol.kingcatalog.ui.components.BookCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherAuthorsScreen(
    vm: OtherAuthorsViewModel = viewModel(),
    onBookClick: (Int) -> Unit = {},
) {
    val booksByAuthor by vm.booksByAuthor.collectAsState()
    val query by vm.searchQuery.collectAsState()

    // Per-author expanded state; defaults to true (expanded)
    val expandedState = remember { mutableStateMapOf<String, Boolean>() }

    var selectedBook by remember { mutableStateOf<Book?>(null) }
    val sheetState = rememberModalBottomSheetState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            OutlinedTextField(
                value = query,
                onValueChange = vm::onQueryChange,
                placeholder = { Text("Search other authors…") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (booksByAuthor.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No other authors yet.\nImport data to populate this list.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    booksByAuthor.forEach { (author, books) ->
                        val isExpanded = expandedState.getOrDefault(author, true)

                        // Collapsible full-width author header
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
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                )
                            }
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
