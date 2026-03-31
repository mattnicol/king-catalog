package com.mattnicol.kingcatalog.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mattnicol.kingcatalog.data.model.Book
import com.mattnicol.kingcatalog.ui.components.BookCard

@Composable
fun HomeScreen(vm: HomeViewModel = viewModel()) {
    val readingNow by vm.readingNow.collectAsState(initial = emptyList())
    val recentlyRead by vm.recentlyRead.collectAsState(initial = emptyList())
    val nextUp by vm.nextUp.collectAsState(initial = emptyList())

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                HomeSection(
                    title = "Currently Reading",
                    books = readingNow,
                    emptyMessage = "Nothing in progress",
                )
            }
            item {
                HomeSection(
                    title = "Recently Read",
                    books = recentlyRead,
                    emptyMessage = "No recently read books",
                )
            }
            item {
                HomeSection(
                    title = "Next Up",
                    books = nextUp,
                    emptyMessage = "Reading list is empty",
                )
            }
        }
    }
}

@Composable
private fun HomeSection(title: String, books: List<Book>, emptyMessage: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        if (books.isEmpty()) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(books) { book ->
                    BookCard(
                        book = book,
                        modifier = Modifier.fillMaxWidth(0.6f),
                    )
                }
            }
        }
    }
}
