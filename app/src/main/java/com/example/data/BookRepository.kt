package com.example.data

import com.example.data.database.BookDao
import com.example.data.database.BookmarkEntity
import com.example.data.database.ChapterProgressEntity
import com.example.data.database.ReadingGoalEntity
import com.example.data.database.UserNoteEntity
import com.example.data.model.Chapter
import com.example.data.model.DuaItem
import com.example.data.model.QuoteOfDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookRepository(private val bookDao: BookDao) {

    val chapters: List<Chapter> = SampleBookData.chapters
    val categories: List<String> = SampleBookData.categories
    val quotesOfDay: List<QuoteOfDay> = SampleBookData.quotesOfDay
    val importantDuas: List<DuaItem> = SampleBookData.importantDuas

    fun getChapterById(id: Int): Chapter {
        return chapters.firstOrNull { it.id == id } ?: chapters.first()
    }

    fun searchChapters(query: String, category: String = "সকল অধ্যায়"): List<Chapter> {
        val categoryFiltered = if (category == "সকল অধ্যায়") {
            chapters
        } else {
            chapters.filter { it.category == category }
        }

        if (query.isBlank()) return categoryFiltered

        val cleanQuery = query.trim().lowercase()
        return categoryFiltered.filter {
            it.titleBangla.lowercase().contains(cleanQuery) ||
                    it.subtitleBangla.lowercase().contains(cleanQuery) ||
                    it.content.lowercase().contains(cleanQuery) ||
                    it.category.lowercase().contains(cleanQuery)
        }
    }

    // Room DB Flow Operations
    val allProgress: Flow<List<ChapterProgressEntity>> = bookDao.getAllProgress()
    val lastReadProgress: Flow<ChapterProgressEntity?> = bookDao.getLastReadChapterProgress()
    val bookmarks: Flow<List<BookmarkEntity>> = bookDao.getAllBookmarks()
    val notes: Flow<List<UserNoteEntity>> = bookDao.getAllNotes()
    val readingGoal: Flow<ReadingGoalEntity?> = bookDao.getReadingGoal()

    fun isChapterBookmarked(chapterId: Int): Flow<Boolean> {
        return bookDao.isChapterBookmarked(chapterId)
    }

    suspend fun updateChapterProgress(chapterId: Int, scrollProgress: Float, isCompleted: Boolean = false) {
        val entity = ChapterProgressEntity(
            chapterId = chapterId,
            lastReadTimestamp = System.currentTimeMillis(),
            scrollPositionProgress = scrollProgress,
            isCompleted = isCompleted
        )
        bookDao.saveProgress(entity)
    }

    suspend fun toggleBookmark(chapter: Chapter, excerpt: String = "") {
        val isBookmarked = bookDao.getProgressForChapter(chapter.id) != null
        // Let's check bookmark state directly
        bookDao.removeBookmarkByChapter(chapter.id)
        val newBookmark = BookmarkEntity(
            chapterId = chapter.id,
            chapterTitle = chapter.titleBangla,
            excerpt = if (excerpt.isNotBlank()) excerpt else chapter.subtitleBangla,
            timestamp = System.currentTimeMillis()
        )
        bookDao.addBookmark(newBookmark)
    }

    suspend fun addBookmark(chapterId: Int, chapterTitle: String, excerpt: String, note: String = "") {
        val entity = BookmarkEntity(
            chapterId = chapterId,
            chapterTitle = chapterTitle,
            excerpt = excerpt,
            note = note,
            timestamp = System.currentTimeMillis()
        )
        bookDao.addBookmark(entity)
    }

    suspend fun removeBookmark(chapterId: Int) {
        bookDao.removeBookmarkByChapter(chapterId)
    }

    suspend fun addNote(chapterId: Int, chapterTitle: String, noteTitle: String, noteContent: String) {
        val entity = UserNoteEntity(
            chapterId = chapterId,
            chapterTitle = chapterTitle,
            noteTitle = noteTitle,
            noteContent = noteContent,
            timestamp = System.currentTimeMillis()
        )
        bookDao.saveNote(entity)
    }

    suspend fun deleteNote(note: UserNoteEntity) {
        bookDao.deleteNote(note)
    }

    suspend fun incrementReadingMinutes(minutes: Int) {
        val currentGoal = bookDao.getReadingGoal()
        val existing = currentGoal?.let { null } // We will update goal
        val newGoal = ReadingGoalEntity(
            id = 1,
            dailyTargetMinutes = 15,
            todayMinutesRead = minutes,
            streakDays = 3,
            lastReadDate = "আজ"
        )
        bookDao.saveReadingGoal(newGoal)
    }
}
