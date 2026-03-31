package com.mattnicol.kingcatalog.data.model

data class Book(
    val id: Int,
    val title: String,
    val author: String,
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
    val isOwned: Boolean,
    val isRead: Boolean,
    val isReadingNow: Boolean,
    val isOnReadingList: Boolean,
    val lastStatusChanged: Long?,
    val notes: String?,
)

fun Book.matchesFilter(
    query: String,
    storyTypeFilter: String?,
    genreFilter: String?,
    decadeFilter: Int?,
    readFilter: Boolean?,
): Boolean {
    if (query.isNotBlank() && !title.contains(query, ignoreCase = true)) return false
    if (storyTypeFilter != null && storyType != storyTypeFilter) return false
    if (genreFilter != null && !keywords.any { it.equals(genreFilter, ignoreCase = true) }) return false
    if (decadeFilter != null && decade != decadeFilter) return false
    if (readFilter != null && isRead != readFilter) return false
    return true
}
