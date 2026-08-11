package com.example.ui

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BookRepository
import com.example.data.database.AppDatabase
import com.example.data.database.BookmarkEntity
import com.example.data.database.ChapterProgressEntity
import com.example.data.database.ReadingGoalEntity
import com.example.data.database.UserNoteEntity
import com.example.data.model.Chapter
import com.example.data.model.QuoteOfDay
import com.example.data.model.ReaderTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

sealed class Screen {
    object Dashboard : Screen()
    data class Reader(val chapterId: Int) : Screen()
    object Bookmarks : Screen()
    object Notes : Screen()
    object Duas : Screen()
    object Settings : Screen()
}

class MainViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val db = AppDatabase.getDatabase(application)
    val repository = BookRepository(db.bookDao())

    // Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Search and Category Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("সকল অধ্যায়")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Reader Preferences
    private val _fontSizeSp = MutableStateFlow(20)
    val fontSizeSp: StateFlow<Int> = _fontSizeSp.asStateFlow()

    private val _readerTheme = MutableStateFlow(ReaderTheme.DARK)
    val readerTheme: StateFlow<ReaderTheme> = _readerTheme.asStateFlow()

    private val _isDarkAppTheme = MutableStateFlow(true)
    val isDarkAppTheme: StateFlow<Boolean> = _isDarkAppTheme.asStateFlow()

    // Text To Speech
    private var tts: TextToSpeech? = TextToSpeech(application, this)
    private val _isTtsSpeaking = MutableStateFlow(false)
    val isTtsSpeaking: StateFlow<Boolean> = _isTtsSpeaking.asStateFlow()

    // Current Chapter in Reader
    private val _activeChapter = MutableStateFlow(repository.chapters.first())
    val activeChapter: StateFlow<Chapter> = _activeChapter.asStateFlow()

    // Random Daily Quote
    val dailyQuote: QuoteOfDay = repository.quotesOfDay.random()

    // Flow derived filtered chapters
    val filteredChapters: StateFlow<List<Chapter>> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        repository.searchChapters(query, category)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = repository.chapters
    )

    // DB Flows
    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notes: StateFlow<List<UserNoteEntity>> = repository.notes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val lastReadProgress: StateFlow<ChapterProgressEntity?> = repository.lastReadProgress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val readingGoal: StateFlow<ReadingGoalEntity?> = repository.readingGoal.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReadingGoalEntity(1, 15, 8, 5, "আজ")
    )

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        if (screen is Screen.Reader) {
            _activeChapter.value = repository.getChapterById(screen.chapterId)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setFontSize(size: Int) {
        _fontSizeSp.value = size.coerceIn(14, 36)
    }

    fun setReaderTheme(theme: ReaderTheme) {
        _readerTheme.value = theme
    }

    fun toggleAppTheme() {
        _isDarkAppTheme.value = !_isDarkAppTheme.value
    }

    fun saveChapterProgress(chapterId: Int, progress: Float) {
        viewModelScope.launch {
            repository.updateChapterProgress(chapterId, progress, progress >= 0.9f)
        }
    }

    fun toggleBookmark(chapter: Chapter, excerpt: String = "") {
        viewModelScope.launch {
            repository.addBookmark(chapter.id, chapter.titleBangla, excerpt.ifBlank { chapter.subtitleBangla })
        }
    }

    fun removeBookmark(chapterId: Int) {
        viewModelScope.launch {
            repository.removeBookmark(chapterId)
        }
    }

    fun addNote(chapterId: Int, chapterTitle: String, title: String, content: String) {
        viewModelScope.launch {
            repository.addNote(chapterId, chapterTitle, title, content)
        }
    }

    fun deleteNote(note: UserNoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // TTS Operations
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("bn", "BD")
        }
    }

    fun toggleTtsSpeech(text: String) {
        if (_isTtsSpeaking.value) {
            tts?.stop()
            _isTtsSpeaking.value = false
        } else {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "VOYABOHOTA_TTS")
            _isTtsSpeaking.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
