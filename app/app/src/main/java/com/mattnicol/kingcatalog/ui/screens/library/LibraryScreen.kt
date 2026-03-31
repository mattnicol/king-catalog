package com.mattnicol.kingcatalog.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mattnicol.kingcatalog.data.model.Book
import com.mattnicol.kingcatalog.ui.components.BookCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(vm: LibraryViewModel = viewModel()) {
    val books by vm.ownedBooks.collectAsState()
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    val sheetState = rememberModalBottomSheetState()

    Surface(modifier = Modifier.fillMaxSize()) {
        if (books.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Your library is empty.\n\nLong-press any book in Books to add it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp),
                )
            }
        } else {
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

        selectedBook?.let { book ->
            ModalBottomSheet(
                onDismissRequest = { selectedBook = null },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                LibraryActionSheet(
                    book = book,
                    onDismiss = { selectedBook = null },
                    onReadingNow = { vm.setReadingNow(book, !book.isReadingNow); selectedBook = null },
                    onMarkRead = { vm.setRead(book, !book.isRead); selectedBook = null },
                    onToggleReadingList = {
                        vm.setOnReadingList(book, !book.isOnReadingList); selectedBook = null
                    },
                    onRemoveFromLibrary = { vm.setOwned(book, false); selectedBook = null },
                )
            }
        }
    }
}

@Composable
private fun LibraryActionSheet(
    book: Book,
    onDismiss: () -> Unit,
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
        TextButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        ) { Text("Cancel") }
    }
}
