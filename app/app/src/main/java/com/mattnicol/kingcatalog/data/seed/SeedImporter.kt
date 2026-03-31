package com.mattnicol.kingcatalog.data.seed

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mattnicol.kingcatalog.data.db.entity.BookEntity
import com.mattnicol.kingcatalog.data.model.Adaptation

/**
 * Reads king_catalog.json from assets and maps it to [BookEntity] rows.
 * Designed to be called once on first launch (when DB is empty).
 */
object SeedImporter {

    fun load(context: Context): List<BookEntity> {
        val json = context.assets.open("king_catalog.json").bufferedReader().readText()
        val array = Gson().fromJson(json, JsonArray::class.java)
        return array.mapNotNull { element ->
            runCatching { parseEntry(element.asJsonObject) }.getOrNull()
        }
    }

    private fun parseEntry(obj: JsonObject): BookEntity {
        val year = obj.getIntOrNull("year")
        val decade = year?.let { (it / 10) * 10 }

        val adaptations = obj.getAsJsonArray("adaptations")?.map { elem ->
            val a = elem.asJsonObject
            Adaptation(
                year = a.getIntOrNull("year"),
                yearEnd = a.getIntOrNull("year_end"),
                title = a.getStringOrNull("title"),
                format = a.getStringOrNull("format"),
                timesSeen = a.getIntOrNull("times_seen"),
                status = a.getStringOrNull("status"),
                want = a.get("want")?.asBoolean ?: false,
                raw = a.getStringOrNull("raw"),
            )
        } ?: emptyList()

        val keywords = obj.getAsJsonArray("keywords")
            ?.mapNotNull { it.asString } ?: emptyList()

        val genres = obj.getAsJsonArray("genres")
            ?.mapNotNull { it.asString } ?: emptyList()

        val asBachman = obj.get("as_bachman")?.asBoolean ?: false

        val childIds = obj.getAsJsonArray("child_ids")
            ?.mapNotNull { it.asInt } ?: emptyList()

        return BookEntity(
            id = obj.get("id").asInt,
            title = obj.get("title").asString,
            author = "Stephen King",
            asBachman = asBachman,
            year = year,
            decade = decade,
            wordCount = obj.getIntOrNull("word_count"),
            audibleMinutes = obj.get("audible_minutes")
                ?.takeIf { !it.isJsonNull }?.asDouble?.toInt(),
            storyType = obj.getStringOrNull("story_type") ?: "unknown",
            keywords = keywords,
            genres = genres,
            isCollectionParent = obj.get("is_collection_parent")?.asBoolean ?: false,
            collection = obj.getStringOrNull("collection"),
            collectionId = obj.getIntOrNull("collection_id"),
            childIds = childIds,
            hasAdaptation = obj.get("has_adaptation")?.asBoolean ?: false,
            adaptations = adaptations,
            imdbUrl = null,
            coverLocalPath = obj.getStringOrNull("cover_local_path")?.substringAfterLast('/'),
            coverCandidateUrl = obj.getStringOrNull("cover_candidate_url"),
            notes = obj.getStringOrNull("notes"),
        )
    }

    private fun JsonObject.getStringOrNull(key: String): String? =
        get(key)?.takeIf { !it.isJsonNull }?.asString

    private fun JsonObject.getIntOrNull(key: String): Int? =
        get(key)?.takeIf { !it.isJsonNull }?.asInt
}
