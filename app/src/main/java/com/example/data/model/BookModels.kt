package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class ReaderTheme(val titleBangla: String) {
    LIGHT("লাইট মোড"),
    DARK("ডাইনামিক নাইট"),
    SEPIA("সেপিয়া (চোখের স্বস্তি)"),
    EMERALD("সমুদ্র সবুজ")
}

data class Chapter(
    val id: Int,
    val numberBangla: String,
    val titleBangla: String,
    val subtitleBangla: String,
    val category: String,
    val estimatedReadTime: String,
    val content: String,
    val keyTakeaway: String,
    val duas: List<DuaItem> = emptyList()
)

data class DuaItem(
    val title: String,
    val arabic: String,
    val pronunciation: String,
    val translation: String,
    val reference: String
)

data class QuoteOfDay(
    val quoteBangla: String,
    val reference: String,
    val category: String
)
