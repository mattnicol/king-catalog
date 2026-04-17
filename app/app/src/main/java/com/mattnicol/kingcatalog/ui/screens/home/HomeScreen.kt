package com.mattnicol.kingcatalog.ui.screens.home

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mattnicol.kingcatalog.data.model.Book
import com.mattnicol.kingcatalog.ui.components.BookActionSheet

private val CARD_WIDTH = 130.dp
private val COVER_HEIGHT = 185.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel = viewModel(),
    onBookClick: (Int) -> Unit = {},
) {
    val readingNow by vm.readingNow.collectAsState(initial = emptyList())
    val recentlyRead by vm.recentlyRead.collectAsState(initial = emptyList())
    val nextUp by vm.nextUp.collectAsState(initial = emptyList())

    var selectedBook by remember { mutableStateOf<Book?>(null) }
    val sheetState = rememberModalBottomSheetState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                HomeSection(
                    title = "Currently Reading",
                    books = readingNow,
                    emptyMessage = "Nothing in progress",
                    onBookClick = onBookClick,
                    onLongClick = { selectedBook = it },
                )
            }
            item {
                HomeSection(
                    title = "Recently Read",
                    books = recentlyRead,
                    emptyMessage = "No recently read books",
                    onBookClick = onBookClick,
                    onLongClick = { selectedBook = it },
                )
            }
            item {
                HomeSection(
                    title = "Next Up",
                    books = nextUp,
                    emptyMessage = "Reading list is empty",
                    onBookClick = onBookClick,
                    onLongClick = { selectedBook = it },
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
            BookActionSheet(
                book = book,
                onDismiss = { selectedBook = null },
                onAddToLibrary = { _ -> vm.onToggleOwned(book); selectedBook = null },
                onReadingNow = { vm.onReadingNow(book); selectedBook = null },
                onMarkRead = { vm.onMarkRead(book); selectedBook = null },
                onToggleReadingList = { vm.onToggleReadingList(book); selectedBook = null },
                onRemoveFromLibrary = { vm.onToggleOwned(book); selectedBook = null },
            )
        }
    }
}

@Composable
private fun HomeSection(
    title: String,
    books: List<Book>,
    emptyMessage: String,
    onBookClick: (Int) -> Unit,
    onLongClick: (Book) -> Unit,
) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        HomeSectionHeader(title = title, count = books.size)

        if (books.isEmpty()) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 8.dp),
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(books, key = { it.id }) { book ->
                    HomeMiniCard(
                        book = book,
                        onClick = { onBookClick(book.id) },
                        onLongClick = { onLongClick(book) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(title: String, count: Int) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            if (count > 0) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            thickness = 1.dp,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeMiniCard(
    book: Book,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .width(CARD_WIDTH)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column {
            val imageModel: Any? = when {
                book.coverLocalPath != null ->
                    Uri.parse("file:///android_asset/covers/${book.coverLocalPath}")
                book.coverCandidateUrl != null -> book.coverCandidateUrl
                else -> null
            }

            Box(
                modifier = Modifier
                    .width(CARD_WIDTH)
                    .height(COVER_HEIGHT),
            ) {
                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = book.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = book.title.take(1),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                // Bottom gradient scrim over cover
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f),
                                ),
                                startY = COVER_HEIGHT.value * 0.55f,
                            )
                        ),
                )
            }

            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                book.year?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = it.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    )
                }
                book.goodreadsRating?.let { rating ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "★ ${"%.2f".format(rating)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFD4A017),
                        ),
                    )
                }
            }
        }
    }
}
