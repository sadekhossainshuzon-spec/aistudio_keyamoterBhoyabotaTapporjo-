package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapter_progress")
data class ChapterProgressEntity(
    @PrimaryKey val chapterId: Int,
    val lastReadTimestamp: Long,
    val scrollPositionProgress: Float = 0f,
    val isCompleted: Boolean = false
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chapterId: Int,
    val chapterTitle: String,
    val excerpt: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_notes")
data class UserNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chapterId: Int,
    val chapterTitle: String,
    val noteTitle: String,
    val noteContent: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_goal")
data class ReadingGoalEntity(
    @PrimaryKey val id: Int = 1,
    val dailyTargetMinutes: Int = 15,
    val todayMinutesRead: Int = 0,
    val streakDays: Int = 1,
    val lastReadDate: String = ""
)
