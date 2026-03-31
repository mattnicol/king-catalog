package com.mattnicol.kingcatalog.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mattnicol.kingcatalog.data.model.Book
import com.mattnicol.kingcatalog.ui.components.BookCard

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(vm: LibraryViewModel = viewModel()) {
    val books by vm.ownedBooks.collectAsState()
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    val sheetState = rememberModalBottomSheetState()

    Surface(modifier = Modifier.fillMaxSize()) {
        if (books.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your library is empty.\nLong-press any book in Books to add it.")
            }
        } else {
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
                BookActionSheet(
                    book = book,
                    onDismiss = { selectedBook = null },
                    onReadingNow = { vm.setReadingNow(book, !book.isReadingNow); selectedBook = null },
                    onMarkRead = { vm.setRead(book, !book.isRead); selectedBook = null },
                    onToggleLibrary = { vm.setOwned(book, !book.isOwned); selectedBook = null },
                    onToggleReadingList = { vm.setOnReadingList(book, !book.isOnReadingList); selectedBook = null },
                )
            }
        }
    }
}

@Composable
private fun BookActionSheet(
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
