package com.mattnicol.kingcatalog.ui.screens.series

import android.app.Application
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mattnicol.kingcatalog.KingCatalogApp
import com.mattnicol.kingcatalog.data.model.Book
import com.mattnicol.kingcatalog.data.model.Connection
import com.mattnicol.kingcatalog.ui.components.BookCard
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// ─────────────────────────────────────────────────────────────────────────────
// Data structures
// ─────────────────────────────────────────────────────────────────────────────

/** A single entry in a connection group's list (book + the relevant connection). */
data class GroupEntry(val book: Book, val connection: Connection)

/** All data needed to render one connection group. */
data class ConnectionGroup(
    val name: String,
    val kind: String,
    val coreEntries: List<GroupEntry>,
    val supplementalEntries: List<GroupEntry>,
    val totalBooks: Int,
    val hasSpoilers: Boolean,
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class SeriesViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as KingCatalogApp).bookRepository

    /** Map of group-name → ConnectionGroup, sorted alphabetically. */
    val groups: StateFlow<List<ConnectionGroup>> = repo.observeWithConnections()
        .map { books -> buildGroups(books) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private fun buildGroups(books: List<Book>): List<ConnectionGroup> {
        // Build group-name → list of (book, connection) pairs, excluding pure genre groups
        val genreGroups = setOf("Horror", "Mystery/thriller/crime", "Supernatural/fantasy")
        val raw = mutableMapOf<String, MutableList<GroupEntry>>()
        for (book in books) {
            for (conn in book.connections) {
                if (conn.group in genreGroups) continue
                raw.getOrPut(conn.group) { mutableListOf() }
                    .add(GroupEntry(book, conn))
            }
        }
        return raw.entries
            .map { (name, entries) ->
                val kind = entries.firstOrNull()?.connection?.kind ?: "connection"
                val core = entries
                    .filter { it.connection.role == "core" }
                    .sortedWith(compareBy(nullsLast()) { it.connection.order })
                val supplemental = entries
                    .filter { it.connection.role != "core" }
                    .sortedWith(compareBy(nullsLast()) { it.connection.order })
                ConnectionGroup(
                    name = name,
                    kind = kind,
                    coreEntries = core,
                    supplementalEntries = supplemental,
                    totalBooks = entries.size,
                    hasSpoilers = entries.any { it.connection.spoiler },
                )
            }
            .sortedWith(compareBy { groupSortPriority(it.name) })
    }

    private fun groupSortPriority(name: String): String {
        val n = name.lowercase()
        return when {
            n.contains("dark tower")          -> "0"
            n.contains("bill hodges")         -> "1"
            n.contains("gwendy")              -> "2"
            n.contains("bachman")             -> "3"
            n.contains("duology")             -> "4_$name"
            n.contains("castle rock")         -> "5"
            else                              -> "6_$name"
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Groups list screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SeriesScreen(
    vm: SeriesViewModel = viewModel(),
    onGroupClick: (String) -> Unit = {},
) {
    val groups by vm.groups.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        if (groups.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No series or connections data yet.\n\n" +
                           "Run enrich.py with --fields connections to populate this.",
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
                item {
                    Text(
                        text = "Series & Connections",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    Text(
                        text = "${groups.size} groups",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(groups, key = { it.name }) { group ->
                    ConnectionGroupCard(group = group, onClick = { onGroupClick(group.name) })
                }
            }
        }
    }
}

@Composable
private fun ConnectionGroupCard(group: ConnectionGroup, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = group.kind.replace('_', ' ').replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    )
                    Text(
                        text = "${group.totalBooks} book${if (group.totalBooks != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    if (group.hasSpoilers) {
                        Text(
                            text = "· has spoilers",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontStyle = FontStyle.Italic,
                        )
                    }
                }
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Group detail screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesGroupDetailScreen(
    groupName: String,
    vm: SeriesViewModel = viewModel(),
    onBack: () -> Unit,
    onBookClick: (Int) -> Unit,
) {
    val groups by vm.groups.collectAsState()
    val group = groups.firstOrNull { it.name == groupName }
    var showSpoilers by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(groupName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSpoilers = !showSpoilers }) {
                        Icon(
                            imageVector = if (showSpoilers) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showSpoilers) "Hide spoilers" else "Show spoilers",
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (group == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Group not found.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Group header
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = group.kind.replace('_', ' ').replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "· ${group.totalBooks} book${if (group.totalBooks != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            // Core entries
            val visibleCore = group.coreEntries.filter { showSpoilers || !it.connection.spoiler }
            if (visibleCore.isNotEmpty()) {
                item {
                    Text(
                        text = "CORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
                items(visibleCore, key = { "core_${it.book.id}" }) { entry ->
                    GroupEntryRow(
                        entry = entry,
                        showSpoilers = showSpoilers,
                        onBookClick = onBookClick,
                    )
                }
            }

            // Supplemental entries
            val visibleSupp = group.supplementalEntries.filter { showSpoilers || !it.connection.spoiler }
            if (visibleSupp.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "SUPPLEMENTAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
                items(visibleSupp, key = { "supp_${it.book.id}" }) { entry ->
                    GroupEntryRow(
                        entry = entry,
                        showSpoilers = showSpoilers,
                        onBookClick = onBookClick,
                    )
                }
            }

            // Hidden spoiler count
            val hiddenSpoilerCount = (group.coreEntries + group.supplementalEntries)
                .count { it.connection.spoiler }
            if (!showSpoilers && hiddenSpoilerCount > 0) {
                item {
                    Text(
                        text = "$hiddenSpoilerCount spoiler entr${if (hiddenSpoilerCount != 1) "ies" else "y"} hidden — tap the eye icon to reveal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupEntryRow(
    entry: GroupEntry,
    showSpoilers: Boolean,
    onBookClick: (Int) -> Unit,
) {
    Column {
        entry.connection.order?.let { order ->
            Text(
                text = "#$order",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 2.dp),
            )
        }
        BookCard(
            book = entry.book,
            onClick = { onBookClick(entry.book.id) },
        )
        entry.connection.note
            ?.takeIf { !it.startsWith("Candidate from local seed") }
            ?.let { note ->
                if (showSpoilers || !entry.connection.spoiler) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                }
            }
    }
}
