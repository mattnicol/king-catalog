package com.mattnicol.kingcatalog.data.model

data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val asBachman: Boolean = false,
    val year: Int?,
    val decade: Int?,
    val wordCount: Int?,
    val audibleMinutes: Int?,
    val storyType: String,
    val keywords: List<String>,
    val genres: List<String>,
    val isCollectionParent: Boolean,
    val collection: String?,
    val collectionId: Int?,
    val childIds: List<Int>,
    val hasAdaptation: Boolean,
    val adaptations: List<Adaptation>,
    val imdbUrl: String?,
    val coverLocalPath: String?,
    val coverCandidateUrl: String?,
    val goodreadsRating: Float?,
    val goodreadsRatingsCount: Int?,
    val isOwned: Boolean,
    val isRead: Boolean,
    val isReadingNow: Boolean,
    val isOnReadingList: Boolean,
    val lastStatusChanged: Long?,
    val notes: String?,
)

fun Book.matchesFilter(
    query: String,
    storyTypeFilter: Set<String>,
    genreFilter: Set<String>,
    keywordFilter: Set<String>,
    decadeFilter: Set<Int>,
    bachamanFilter: Boolean?,
    readFilter: Boolean?,
    inLibraryFilter: Boolean?,
): Boolean {
    if (query.isNotBlank() && !title.contains(query, ignoreCase = true)) return false
    if (storyTypeFilter.isNotEmpty() && storyType !in storyTypeFilter) return false
    if (genreFilter.isNotEmpty() && genres.none { it in genreFilter }) return false
    if (keywordFilter.isNotEmpty() && !keywords.any { it in keywordFilter }) return false
    if (decadeFilter.isNotEmpty() && decade !in decadeFilter) return false
    if (bachamanFilter != null && asBachman != bachamanFilter) return false
    if (readFilter != null && isRead != readFilter) return false
    if (inLibraryFilter != null && isOwned != inLibraryFilter) return false
    return true
}
