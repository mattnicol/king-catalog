package com.mattnicol.kingcatalog.data.model

data class Adaptation(
    val year: Int?,
    val yearEnd: Int?,
    val title: String?,
    val format: String?,
    val timesSeen: Int?,
    val status: String?,
    val want: Boolean,
    val raw: String?,
)
