package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    // Chapter Progress
    @Query("SELECT * FROM chapter_progress")
    fun getAllProgress(): Flow<List<ChapterProgressEntity>>

    @Query("SELECT * FROM chapter_progress WHERE chapterId = :chapterId")
    suspend fun getProgressForChapter(chapterId: Int): ChapterProgressEntity?

    @Query("SELECT * FROM chapter_progress ORDER BY lastReadTimestamp DESC LIMIT 1")
    fun getLastReadChapterProgress(): Flow<ChapterProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: ChapterProgressEntity)

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE chapterId = :chapterId LIMIT 1)")
    fun isChapterBookmarked(chapterId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE chapterId = :chapterId")
    suspend fun removeBookmarkByChapter(chapterId: Int)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    // Notes
    @Query("SELECT * FROM user_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<UserNoteEntity>>

    @Query("SELECT * FROM user_notes WHERE chapterId = :chapterId ORDER BY timestamp DESC")
    fun getNotesForChapter(chapterId: Int): Flow<List<UserNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNote(note: UserNoteEntity)

    @Delete
    suspend fun deleteNote(note: UserNoteEntity)

    // Reading Goal
    @Query("SELECT * FROM reading_goal WHERE id = 1")
    fun getReadingGoal(): Flow<ReadingGoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReadingGoal(goal: ReadingGoalEntity)
}
