package com.sujok.rahbar

enum class HandZone {
    THUMB, FINGERS, PALM_CENTER, PALM_LOW, WRIST, FOOT
}

data class LocalGuide(
    val id: String,
    val titleUr: String,
    val titleEn: String,
    val pointUr: String,
    val pointEn: String,
    val zone: HandZone,
    val methodUr: String,
    val aliases: List<String>,
    val topic: List<String>,
    val bans: List<String>,
    val searchEn: String,
    val imageQuery: String,
)

data class WebSource(
    val title: String,
    val url: String,
    val snippet: String,
    val snippetUr: String,
    val score: Int,
)

data class SearchResult(
    val query: String,
    val guide: LocalGuide?,
    val consensusUr: String,
    val methodUr: String,
    val images: List<String>,
    val sources: List<WebSource>,
)
