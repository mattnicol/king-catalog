package com.mattnicol.kingcatalog.data.model

/**
 * A single entry in the "Series / Connections" feature.
 *
 * [group]  — the named group, e.g. "The Dark Tower", "Castle Rock", "Holly Gibney"
 * [kind]   — series | trilogy | cycle | universe | connection | easter_egg
 * [role]   — core (reading-order entry) | supplemental (related but optional)
 * [order]  — display order within the group; null means unordered
 * [spoiler]— when true the [note] should be hidden by default
 * [note]   — optional prose description or context
 */
data class Connection(
    val group: String,
    val kind: String,
    val role: String,
    val order: Int?,
    val spoiler: Boolean,
    val note: String?,
)
