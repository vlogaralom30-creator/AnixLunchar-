package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.AppActionDialog
import com.example.ui.components.AppDrawerSheet
import com.example.ui.components.CustomizationBottomSheet
import com.example.ui.components.FavoritesVerticalList
import com.example.ui.components.LauncherClockWidget
import com.example.ui.components.QuickSearchOverlay
import com.example.util.SoundFeedbackUtil
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherScreen(
    viewModel: LauncherViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Clock ticker every 10s
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateDateTime()
            delay(10_000L)
        }
    }

    // Determine apps to display on home list (either all installed apps or favorites based on preference)
    val displayApps = if (state.preferences.showAllAppsOnHome) {
        state.allInstalledApps.ifEmpty { state.favoriteApps }
    } else {
        state.favoriteApps
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("launcher_screen")
    ) {
        // 1. Full-screen Wallpaper
        Image(
            painter = painterResource(id = state.preferences.selectedWallpaper.drawableRes),
            contentDescription = state.preferences.selectedWallpaper.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dim overlay if configured
        if (state.preferences.dimBackgroundPercent > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = state.preferences.dimBackgroundPercent / 100f))
            )
        }

        // Left vertical dock frosted scrim (matches the vertical divider / soft shadow in reference design)
        if (state.preferences.useLeftDockScrim) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.55f)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0x730A1210),
                                Color(0x3B0D1614),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Empty wallpaper area detector on the right side: handles swipe gestures and long-press customization
        var rightDragY by remember { mutableFloatStateOf(0f) }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.35f)
                .align(Alignment.CenterEnd)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        viewModel.closeAllOverlays()
                    },
                    onLongClick = {
                        if (state.preferences.enableClickSound) {
                            SoundFeedbackUtil.playClickSoundAndHaptic(context)
                        }
                        viewModel.openCustomization()
                    }
                )
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { rightDragY = 0f },
                        onDragEnd = {
                            if (rightDragY < -120f) {
                                if (state.preferences.enableClickSound) {
                                    SoundFeedbackUtil.playClickSoundAndHaptic(context)
                                }
                                viewModel.openAppDrawer()
                            } else if (rightDragY > 120f) {
                                if (state.preferences.enableClickSound) {
                                    SoundFeedbackUtil.playClickSoundAndHaptic(context)
                                }
                                viewModel.openSearch()
                            }
                            rightDragY = 0f
                        },
                        onDragCancel = { rightDragY = 0f },
                        onVerticalDrag = { _, dragAmount -> rightDragY += dragAmount }
                    )
                }
        )

        // Main Foreground UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 28.dp, end = 24.dp, top = 32.dp, bottom = 28.dp)
        ) {
            // Top Digital Clock + Date & Weather
            LauncherClockWidget(
                timeString = state.currentTimeString,
                dateString = state.currentDateString,
                weatherTempString = state.weatherTempString,
                clockSizeSp = state.preferences.clockSizeSp,
                clockPosition = state.preferences.clockPosition,
                clockFontStyle = state.preferences.clockFontStyle,
                showWeather = state.preferences.showWeather,
                onClockClick = {
                    if (state.preferences.enableClickSound) {
                        SoundFeedbackUtil.playClickSoundAndHaptic(context)
                    }
                    viewModel.openSearch()
                },
                modifier = Modifier.testTag("launcher_clock_widget")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Center area: Vertical scrollable apps list & Floating Add Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Vertical Apps list (Scrolls up and down through apps smoothly)
                FavoritesVerticalList(
                    apps = displayApps,
                    preferences = state.preferences,
                    onAppClick = { app ->
                        viewModel.launchApp(app)
                    },
                    onAppLongClick = { app ->
                        viewModel.openAppActionMenu(app)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .testTag("favorites_vertical_list")
                )

                // Floating "+" Button at bottom right of dock
                FloatingActionButton(
                    onClick = {
                        if (state.preferences.enableClickSound) {
                            SoundFeedbackUtil.playClickSoundAndHaptic(context)
                        }
                        viewModel.openAppDrawer()
                    },
                    shape = CircleShape,
                    containerColor = Color(0xCC6F8E83),
                    contentColor = Color(0xFF101C17),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 12.dp, end = 8.dp)
                        .size(56.dp)
                        .testTag("add_app_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Apps / Open Drawer",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // 2. Full App Drawer Sheet
        AppDrawerSheet(
            isOpen = state.isAppDrawerOpen,
            apps = state.filteredSearchApps,
            searchQuery = state.searchQuery,
            preferences = state.preferences,
            onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
            onAppClick = { app ->
                if (state.preferences.enableClickSound) {
                    SoundFeedbackUtil.playClickSoundAndHaptic(context)
                }
                viewModel.launchApp(app)
            },
            onAppLongClick = { app ->
                if (state.preferences.enableClickSound) {
                    SoundFeedbackUtil.playClickSoundAndHaptic(context)
                }
                viewModel.addFavorite(app)
                viewModel.closeAppDrawer()
            },
            onOpenSettings = { viewModel.openCustomization() },
            onDismiss = { viewModel.closeAppDrawer() }
        )

        // 3. Quick Search Overlay (Swipe Down)
        QuickSearchOverlay(
            isOpen = state.isSearchOpen,
            query = state.searchQuery,
            filteredApps = state.filteredSearchApps,
            preferences = state.preferences,
            onQueryChange = { viewModel.onSearchQueryChanged(it) },
            onAppClick = { app ->
                if (state.preferences.enableClickSound) {
                    SoundFeedbackUtil.playClickSoundAndHaptic(context)
                }
                viewModel.launchApp(app)
            },
            onDismiss = { viewModel.closeSearch() }
        )

        // 4. Customization Bottom Sheet (Long press empty wallpaper)
        CustomizationBottomSheet(
            isOpen = state.isCustomizationOpen,
            preferences = state.preferences,
            onPreferencesChange = { viewModel.updatePreferences(it) },
            onDismiss = { viewModel.closeCustomization() }
        )

        // 5. App Action Dialog (Long press app)
        AppActionDialog(
            isOpen = state.isAppActionMenuOpen,
            app = state.selectedAppForAction,
            onLaunch = {
                if (state.preferences.enableClickSound) {
                    SoundFeedbackUtil.playClickSoundAndHaptic(context)
                }
                state.selectedAppForAction?.let { viewModel.launchApp(it) }
            },
            onRemoveFavorite = {
                if (state.preferences.enableClickSound) {
                    SoundFeedbackUtil.playClickSoundAndHaptic(context)
                }
                state.selectedAppForAction?.let { viewModel.removeFavorite(it) }
            },
            onMoveUp = {
                if (state.preferences.enableClickSound) {
                    SoundFeedbackUtil.playClickSoundAndHaptic(context)
                }
                state.selectedAppForAction?.let { viewModel.moveFavoriteUp(it) }
                viewModel.closeAppActionMenu()
            },
            onMoveDown = {
                if (state.preferences.enableClickSound) {
                    SoundFeedbackUtil.playClickSoundAndHaptic(context)
                }
                state.selectedAppForAction?.let { viewModel.moveFavoriteDown(it) }
                viewModel.closeAppActionMenu()
            },
            onDismiss = { viewModel.closeAppActionMenu() }
        )
    }
}
