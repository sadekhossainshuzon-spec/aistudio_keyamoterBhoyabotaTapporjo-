package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DuasScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.theme.VoyabohotaTheme

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val isDarkTheme by viewModel.isDarkAppTheme.collectAsState()
      val currentScreen by viewModel.currentScreen.collectAsState()

      // Handle Back button on secondary screens
      if (currentScreen !is Screen.Dashboard) {
        BackHandler {
          viewModel.navigateTo(Screen.Dashboard)
        }
      }

      VoyabohotaTheme(darkTheme = isDarkTheme) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
            when (screen) {
              is Screen.Dashboard -> DashboardScreen(viewModel = viewModel)
              is Screen.Reader -> ReaderScreen(viewModel = viewModel)
              is Screen.Bookmarks -> BookmarksScreen(viewModel = viewModel)
              is Screen.Notes -> NotesScreen(viewModel = viewModel)
              is Screen.Duas -> DuasScreen(viewModel = viewModel)
              is Screen.Settings -> DashboardScreen(viewModel = viewModel)
            }
          }
        }
      }
    }
  }
}

