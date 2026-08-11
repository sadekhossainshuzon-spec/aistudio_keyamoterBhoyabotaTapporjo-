package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chapter
import com.example.data.model.DuaItem
import com.example.data.model.ReaderTheme
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.ReaderSettingsDialog
import com.example.ui.theme.DarkCanvasBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LightCanvasBackground
import com.example.ui.theme.SepiaBackground

data class ReaderThemeColors(
    val bg: Color,
    val text: Color,
    val cardBg: Color,
    val accent: Color
)

@Composable
fun ReaderScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    val chapter by viewModel.activeChapter.collectAsState()
    val fontSizeSp by viewModel.fontSizeSp.collectAsState()
    val readerTheme by viewModel.readerTheme.collectAsState()
    val isTtsSpeaking by viewModel.isTtsSpeaking.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    val isBookmarked = remember(bookmarks, chapter) {
        bookmarks.any { it.chapterId == chapter.id }
    }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }

    // Color Scheme based on Reader Theme
    val themeColors = when (readerTheme) {
        ReaderTheme.DARK -> ReaderThemeColors(DarkCanvasBackground, DarkTextPrimary, DarkSurface, EmeraldPrimary)
        ReaderTheme.SEPIA -> ReaderThemeColors(SepiaBackground, Color(0xFF432818), Color(0xFFF5E6C8), Color(0xFFB45309))
        ReaderTheme.LIGHT -> ReaderThemeColors(LightCanvasBackground, Color(0xFF0F172A), Color(0xFFFFFFFF), EmeraldPrimary)
        ReaderTheme.EMERALD -> ReaderThemeColors(Color(0xFF0D221A), Color(0xFFECFDF5), Color(0xFF153327), Color(0xFF34D399))
    }
    val bgColor = themeColors.bg
    val textColor = themeColors.text
    val cardBgColor = themeColors.cardBg
    val accentColor = themeColors.accent

    // Scroll Progress Handler
    LaunchedEffect(scrollState.value, scrollState.maxValue) {
        if (scrollState.maxValue > 0) {
            val progress = scrollState.value.toFloat() / scrollState.maxValue.toFloat()
            viewModel.saveChapterProgress(chapter.id, progress)
        }
    }

    if (showSettingsDialog) {
        ReaderSettingsDialog(
            fontSizeSp = fontSizeSp,
            currentTheme = readerTheme,
            onFontSizeChange = { viewModel.setFontSize(it) },
            onThemeChange = { viewModel.setReaderTheme(it) },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("নোট যোগ করুন (অধ্যায় ${chapter.numberBangla})", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("নোটের শিরোনাম") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("আপনার উপলব্ধি বা টীকা...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteContent.isNotBlank()) {
                            viewModel.addNote(
                                chapterId = chapter.id,
                                chapterTitle = chapter.titleBangla,
                                title = noteTitle.ifBlank { "অনুভূতি ও শিক্ষা" },
                                content = noteContent
                            )
                            Toast.makeText(context, "নোট সংরক্ষণ করা হয়েছে", Toast.LENGTH_SHORT).show()
                            noteTitle = ""
                            noteContent = ""
                            showNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            ReaderTopAppBar(
                chapter = chapter,
                isBookmarked = isBookmarked,
                isTtsSpeaking = isTtsSpeaking,
                textColor = textColor,
                onBack = { viewModel.navigateTo(Screen.Dashboard) },
                onToggleBookmark = {
                    if (isBookmarked) {
                        viewModel.removeBookmark(chapter.id)
                        Toast.makeText(context, "বুকমার্ক সরানো হয়েছে", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.toggleBookmark(chapter)
                        Toast.makeText(context, "অধ্যায় বুকমার্ক করা হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                },
                onToggleAudio = {
                    viewModel.toggleTtsSpeech(chapter.content)
                },
                onAddNote = { showNoteDialog = true },
                onOpenSettings = { showSettingsDialog = true }
            )
        },
        bottomBar = {
            ReaderBottomNav(
                currentChapterId = chapter.id,
                totalChapters = viewModel.repository.chapters.size,
                scrollProgress = if (scrollState.maxValue > 0) scrollState.value.toFloat() / scrollState.maxValue.toFloat() else 0f,
                onPrevious = {
                    if (chapter.id > 1) {
                        viewModel.navigateTo(Screen.Reader(chapter.id - 1))
                    }
                },
                onNext = {
                    if (chapter.id < viewModel.repository.chapters.size) {
                        viewModel.navigateTo(Screen.Reader(chapter.id + 1))
                    }
                }
            )
        },
        containerColor = bgColor,
        modifier = modifier.testTag("reader_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Chapter Title Header Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "অধ্যায় ${chapter.numberBangla}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Text(
                            text = "⏱️ আনুমানিক সময়: ${chapter.estimatedReadTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = chapter.titleBangla,
                        fontSize = (fontSizeSp + 4).sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        lineHeight = (fontSizeSp + 10).sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = chapter.subtitleBangla,
                        fontSize = (fontSizeSp - 2).sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
            }

            // Key Takeaway Box
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor.copy(alpha = 0.8f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "মূল শিক্ষা / টেকঅ্যাওয়ে:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = chapter.keyTakeaway,
                            fontSize = (fontSizeSp - 2).sp,
                            color = textColor,
                            lineHeight = (fontSizeSp + 4).sp
                        )
                    }
                }
            }

            // Main Text Content
            Text(
                text = chapter.content,
                fontSize = fontSizeSp.sp,
                color = textColor,
                lineHeight = (fontSizeSp + 12).sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("reader_chapter_content")
            )

            // Embedded Duas Section (if any)
            if (chapter.duas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "গুরুত্বপূর্ণ সম্পর্কিত দু'আসমূহ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                chapter.duas.forEach { dua ->
                    DuaCardItem(
                        dua = dua,
                        cardBgColor = cardBgColor,
                        textColor = textColor,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString("${dua.arabic}\n${dua.pronunciation}\n${dua.translation}"))
                            Toast.makeText(context, "দোয়া কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ReaderTopAppBar(
    chapter: Chapter,
    isBookmarked: Boolean,
    isTtsSpeaking: Boolean,
    textColor: Color,
    onBack: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleAudio: () -> Unit,
    onAddNote: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("reader_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = textColor)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "অধ্যায় ${chapter.numberBangla}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1
                )
                Text(
                    text = chapter.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onToggleAudio,
                modifier = Modifier.testTag("reader_audio_button")
            ) {
                Icon(
                    imageVector = if (isTtsSpeaking) Icons.Default.Pause else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "অডিও শোনেন",
                    tint = if (isTtsSpeaking) GoldAccent else textColor
                )
            }

            IconButton(
                onClick = onToggleBookmark,
                modifier = Modifier.testTag("reader_bookmark_button")
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "বুকমার্ক",
                    tint = if (isBookmarked) GoldAccent else textColor
                )
            }

            IconButton(
                onClick = onAddNote,
                modifier = Modifier.testTag("reader_add_note_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = "নোট লিখুন", tint = textColor)
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("reader_settings_button")
            ) {
                Icon(Icons.Default.FormatSize, contentDescription = "টেক্সট সাইজ", tint = textColor)
            }
        }
    }
}

@Composable
private fun ReaderBottomNav(
    currentChapterId: Int,
    totalChapters: Int,
    scrollProgress: Float,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            LinearProgressIndicator(
                progress = { scrollProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = EmeraldPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onPrevious,
                    enabled = currentChapterId > 1,
                    modifier = Modifier.testTag("reader_prev_chapter_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("পূর্ববর্তী অধ্যায়")
                }

                Text(
                    text = "$currentChapterId / $totalChapters",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(
                    onClick = onNext,
                    enabled = currentChapterId < totalChapters,
                    modifier = Modifier.testTag("reader_next_chapter_button")
                ) {
                    Text("পরবর্তী অধ্যায়")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun DuaCardItem(
    dua: DuaItem,
    cardBgColor: Color,
    textColor: Color,
    onCopy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dua.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
                IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "কপি",
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dua.arabic,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                lineHeight = 34.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "উচ্চারণ: ${dua.pronunciation}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "অর্থ: ${dua.translation}",
                fontSize = 14.sp,
                color = textColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "সূত্র: ${dua.reference}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
            )
        }
    }
}
