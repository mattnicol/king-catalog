package com.mattnicol.kingcatalog.ui.screens.home

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mattnicol.kingcatalog.data.model.Book

private val CARD_WIDTH = 110.dp
private val COVER_HEIGHT = 150.dp

@Composable
fun HomeScreen(vm: HomeViewModel = viewModel()) {
    val readingNow by vm.readingNow.collectAsState(initial = emptyList())
    val recentlyRead by vm.recentlyRead.collectAsState(initial = emptyList())
    val nextUp by vm.nextUp.collectAsState(initial = emptyList())

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
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
            text = title.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
        if (books.isEmpty()) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(books, key = { it.id }) { book ->
                    HomeMiniCard(book = book)
                }
            }
        }
    }
}

@Composable
private fun HomeMiniCard(book: Book) {
    Card(
        modifier = Modifier.width(CARD_WIDTH),
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column {
            // Cover image
            val imageModel: Any? = when {
                book.coverLocalPath != null ->
                    Uri.parse("file:///android_asset/covers/${book.coverLocalPath}")
                book.coverCandidateUrl != null -> book.coverCandidateUrl
                else -> null
            }
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = book.title,
                    modifier = Modifier
                        .width(CARD_WIDTH)
                        .height(COVER_HEIGHT),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(CARD_WIDTH)
                        .height(COVER_HEIGHT)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = book.title.take(1),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            // Title + year
            Column(modifier = Modifier.padding(6.dp)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                )
                book.year?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = it.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}
