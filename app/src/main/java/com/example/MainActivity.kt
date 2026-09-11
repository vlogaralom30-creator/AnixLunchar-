package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.LauncherScreen
import com.example.ui.LauncherViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val launcherViewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    LauncherScreen(viewModel = launcherViewModel)
                }
            }
        }
    }

    /**
     * Handles Android Home Button & Gesture Home navigation when user returns to NXV Launcher.
     * Brings user back to the primary Launcher Home and closes any open overlays seamlessly.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        launcherViewModel.closeAllOverlays()
    }

    override fun onResume() {
        super.onResume()
        launcherViewModel.updateDateTime()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Safe fallback for legacy back dispatch: priority close overlays, stay on Launcher Home
        val state = launcherViewModel.uiState.value
        if (state.isAppActionMenuOpen || state.isCustomizationOpen || state.isSearchOpen || state.isAppDrawerOpen || state.currentPage != 0) {
            launcherViewModel.closeAllOverlays()
        } else {
            // Stay on home screen (standard launcher behavior, do not finish activity)
        }
    }
}
