package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewCarousel
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
import com.example.model.AppActionOrigin
import com.example.model.HomeScreenMode
import com.example.ui.components.AppActionDialog
import com.example.ui.components.AppDrawerSheet
import com.example.ui.components.CreateFolderDialog
import com.example.ui.components.CustomizationBottomSheet
import com.example.ui.components.FavoritesVerticalList
import com.example.ui.components.FolderPickerSheet
import com.example.ui.components.FolderViewSheet
import com.example.ui.components.HomeGridList
import com.example.ui.components.LauncherClockWidget
import com.example.ui.components.LockScreenOverlay
import com.example.ui.components.QuickHomeOptionsSheet
import androidx.compose.material.icons.filled.Lock
import com.example.ui.components.QuickSearchOverlay
import com.example.ui.components.RecentsOverviewSheet
import com.example.ui.components.RenameAppDialog
import com.example.util.SoundFeedbackUtil
import kotlinx.coroutines.delay
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherScreen(
    viewModel: LauncherViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // BACK BEHAVIOR: Clean priority hierarchy
    BackHandler(enabled = true) {
        when {
            state.isRenameDialogOpen -> viewModel.closeRenameDialog()
            state.isCreateFolderDialogOpen -> viewModel.closeCreateFolderDialog()
            state.isFolderPickerOpen -> viewModel.closeFolderPicker()
            state.isFolderViewOpen -> viewModel.closeFolderView()
            state.isQuickHomeOptionsOpen -> viewModel.closeQuickHomeOptions()
            state.isRecentsOpen -> viewModel.closeRecents()
            state.isAppActionMenuOpen -> viewModel.closeAppActionMenu()
            state.isCustomizationOpen -> viewModel.closeCustomization()
            state.isSearchOpen -> viewModel.closeSearch()
            state.isAppDrawerOpen -> viewModel.closeAppDrawer()
            state.currentPage != 0 -> viewModel.setPage(0)
            else -> {
                // On root Launcher Home: safely stay on home
            }
        }
    }

    // Clock ticker every 10s
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateDateTime()
            delay(10_000L)
        }
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

        // Left vertical dock frosted scrim
        if (state.preferences.useLeftDockScrim && state.currentPage == 0) {
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

        // 2. Gesture detector on empty area / wallpaper:
        // - Tap: close overlays
        // - Long Press: open Quick Home Options (Wallpaper, Settings, Add App, Layout, Lock/Unlock)
        // - Swipe Up: open App Drawer
        // - Swipe Down: open Search
        // - Swipe Left/Right: change page
        var dragAccumulatorX by remember { mutableFloatStateOf(0f) }
        var dragAccumulatorY by remember { mutableFloatStateOf(0f) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        viewModel.closeAllOverlays()
                    },
                    onLongClick = {
                        SoundFeedbackUtil.playClickFeedback(context, state.preferences.enableSound, state.preferences.enableHaptic)
                        viewModel.openQuickHomeOptions()
                    }
                )
                .pointerInput(state.currentPage) {
                    detectDragGestures(
                        onDragStart = {
                            dragAccumulatorX = 0f
                            dragAccumulatorY = 0f
                        },
                        onDragEnd = {
                            val absX = abs(dragAccumulatorX)
                            val absY = abs(dragAccumulatorY)
                            val swipeThreshold = 80f

                            if (absY > absX && absY > swipeThreshold) {
                                if (dragAccumulatorY < 0) {
                                    SoundFeedbackUtil.playClickFeedback(context, state.preferences.enableSound, state.preferences.enableHaptic)
                                    viewModel.openAppDrawer()
                                } else {
                                    SoundFeedbackUtil.playClickFeedback(context, state.preferences.enableSound, state.preferences.enableHaptic)
                                    viewModel.openSearch()
                                }
                            } else if (absX > absY && absX > swipeThreshold) {
                                if (dragAccumulatorX < 0) {
                                    SoundFeedbackUtil.playClickFeedback(context, state.preferences.enableSound, state.preferences.enableHaptic)
                                    viewModel.nextPage()
                                } else {
                                    SoundFeedbackUtil.playClickFeedback(context, state.preferences.enableSound, state.preferences.enableHaptic)
                                    viewModel.prevPage()
                                }
                            }
                            dragAccumulatorX = 0f
                            dragAccumulatorY = 0f
                        },
                        onDragCancel = {
                            dragAccumulatorX = 0f
                            dragAccumulatorY = 0f
                        },
                        onDrag = { _, dragAmount ->
                            dragAccumulatorX += dragAmount.x
                            dragAccumulatorY += dragAmount.y
                        }
                    )
                }
        )

        // 3. HOME PAGES
        AnimatedContent(
            targetState = state.currentPage,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) { width -> width } + fadeIn(tween(200))) togetherWith
                            (slideOutHorizontally(
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            ) { width -> -width } + fadeOut(tween(180)))
                } else {
                    (slideInHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) { width -> -width } + fadeIn(tween(200))) togetherWith
                            (slideOutHorizontally(
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            ) { width -> width } + fadeOut(tween(180)))
                }
            },
            label = "home_pages_transition",
            modifier = Modifier.fillMaxSize()
        ) { targetPage ->
            if (targetPage == 0) {
                // PAGE 0: Main Favorites Dock & Clock
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(start = 28.dp, end = 24.dp, top = 32.dp, bottom = 28.dp)
                ) {
                    // Clock Widget
                    LauncherClockWidget(
                        timeString = state.currentTimeString,
                        dateString = state.currentDateString,
                        weatherTempString = state.weatherTempString,
                        clockSizeSp = state.preferences.clockSizeSp,
                        clockPosition = state.preferences.clockPosition,
                        clockFontStyle = state.preferences.clockFontStyle,
                        showWeather = state.preferences.showWeather,
                        onClockClick = {
                            SoundFeedbackUtil.playClickFeedback(context, state.preferences.enableSound, state.preferences.enableHaptic)
                            viewModel.openSearch()
                        },
                        modifier = Modifier.testTag("launcher_clock_widget")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Home Items: Grid or Vertical List
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (state.preferences.homeScreenMode == HomeScreenMode.GRID) {
                            HomeGridList(
                                homeItems = state.homeItems,
                                preferences = state.preferences,
                                onAppClick = { app ->
                                    viewModel.launchApp(app)
                                },
                                onAppLongClick = { app ->
                                    viewModel.openAppActionMenu(app, AppActionOrigin.HOME_SCREEN)
                                },
                                onFolderClick = { folder ->
                                    viewModel.openFolderView(folder)
                                },
                                onReorder = { from, to ->
                                    viewModel.reorderFavoriteApps(from, to)
                                },
                                onMergeAppsIntoFolder = { fromApp, targetApp ->
                                    viewModel.mergeAppsIntoFolder(fromApp, targetApp)
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("home_grid_list")
                            )
                        } else {
                            FavoritesVerticalList(
                                homeItems = state.homeItems,
                                preferences = state.preferences,
                                onAppClick = { app ->
                                    viewModel.launchApp(app)
                                },
                                onAppLongClick = { app ->
                                    viewModel.openAppActionMenu(app, AppActionOrigin.HOME_SCREEN)
                                },
                                onFolderClick = { folder ->
                                    viewModel.openFolderView(folder)
                                },
                                onReorder = { from, to ->
                                    viewModel.reorderFavoriteApps(from, to)
                                },
                                onMergeAppsIntoFolder = { fromApp, targetApp ->
                                    viewModel.mergeAppsIntoFolder(fromApp, targetApp)
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .testTag("favorites_vertical_list")
                            )
                        }

                        // Floating "+" FAB at bottom right
                        FloatingActionButton(
                            onClick = {
                                SoundFeedbackUtil.playClickFeedback(context, state.preferences.enableSound, state.preferences.enableHaptic)
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

                    // Bottom Page Indicator Dots
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 16.dp, height = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.9f))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.35f))
                        )
                    }
                }
            } else {
                // PAGE 1: Glance & Quick Shortcuts
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 32.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 40.dp)
                    ) {
                        Text(
                            text = state.currentTimeString,
                            fontSize = (state.preferences.clockSizeSp + 20).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = state.preferences.clockFontStyle.fontFamily,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.currentDateString,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = state.preferences.clockFontStyle.fontFamily,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = Color(0x33000000),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${state.weatherTempString} • ${state.weatherCondition}",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Quick Launcher Actions Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                SoundFeedbackUtil.playClickFeedback(context, state.preferences.enableSound, state.preferences.enableHaptic)
                                viewModel.openAppDrawer()
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0x33FFFFFF), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = "App Drawer",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                SoundFeedbackUtil.playClickFeedback(context, state.preferences.enableSound, state.preferences.enableHaptic)
                                viewModel.openSearch()
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0x33FFFFFF), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                SoundFeedbackUtil.playClickFeedback(context, state.preferences.enableSound, state.preferences.enableHaptic)
                                viewModel.openRecents()
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0x33FFFFFF), CircleShape)
                                .testTag("page1_recents_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewCarousel,
                                contentDescription = "Recent Apps",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                SoundFeedbackUtil.playClickFeedback(context, state.preferences.enableSound, state.preferences.enableHaptic)
                                viewModel.lockScreen()
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0x33FFFFFF), CircleShape)
                                .testTag("page1_lock_screen_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Screen",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                SoundFeedbackUtil.playClickFeedback(context, state.preferences.enableSound, state.preferences.enableHaptic)
                                viewModel.openCustomization()
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0x33FFFFFF), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Customization",
                                tint = Color.White
                            )
                        }
                    }

                    // Bottom Page Indicator Dots
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.35f))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(width = 16.dp, height = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.9f))
                        )
                    }
                }
            }
        }

        // 4. App Drawer Sheet
        AppDrawerSheet(
            isOpen = state.isAppDrawerOpen,
            apps = state.filteredSearchApps,
            searchQuery = state.searchQuery,
            preferences = state.preferences,
            onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
            onSortOrderChange = { viewModel.setAppSortOrder(it) },
            onAppClick = { app ->
                viewModel.launchApp(app)
            },
            onAppLongClick = { app ->
                viewModel.openAppActionMenu(app, AppActionOrigin.APP_DRAWER)
            },
            onOpenSettings = { viewModel.openCustomization() },
            onDismiss = { viewModel.closeAppDrawer() }
        )

        // 5. Quick Search Overlay
        QuickSearchOverlay(
            isOpen = state.isSearchOpen,
            query = state.searchQuery,
            filteredApps = state.filteredSearchApps,
            preferences = state.preferences,
            onQueryChange = { viewModel.onSearchQueryChanged(it) },
            onAppClick = { app ->
                viewModel.launchApp(app)
            },
            onDismiss = { viewModel.closeSearch() }
        )

        // 6. Quick Home Options Sheet (Long-press empty area)
        QuickHomeOptionsSheet(
            isOpen = state.isQuickHomeOptionsOpen,
            preferences = state.preferences,
            onOpenWallpaper = { viewModel.openCustomization() },
            onOpenHomeSettings = { viewModel.openCustomization() },
            onAddApp = { viewModel.openAppDrawer() },
            onToggleLayout = { viewModel.toggleHomeScreenMode() },
            onToggleLockLayout = { viewModel.toggleLockHomeScreenLayout() },
            onLockScreen = { viewModel.lockScreen() },
            onDismiss = { viewModel.closeQuickHomeOptions() }
        )

        // 7. Customization Bottom Sheet
        CustomizationBottomSheet(
            isOpen = state.isCustomizationOpen,
            preferences = state.preferences,
            onPreferencesChange = { viewModel.updatePreferences(it) },
            onDismiss = { viewModel.closeCustomization() }
        )

        // 8. App Action Menu Dialog
        val selectedApp = state.selectedAppForAction
        val isAppOnHome = selectedApp != null && state.favoriteApps.any { it.packageName == selectedApp.packageName }

        AppActionDialog(
            isOpen = state.isAppActionMenuOpen,
            app = selectedApp,
            origin = state.appActionOrigin,
            isOnHomeScreen = isAppOnHome,
            isLayoutLocked = state.preferences.lockHomeScreenLayout,
            onLaunch = {
                selectedApp?.let { viewModel.launchApp(it) }
            },
            onAppInfo = {
                selectedApp?.let { viewModel.openAppInfo(it) }
            },
            onUninstall = {
                selectedApp?.let { viewModel.uninstallApp(it) }
            },
            onAddToHome = {
                selectedApp?.let { viewModel.addFavorite(it) }
            },
            onRemoveFromHome = {
                selectedApp?.let { viewModel.removeFavorite(it) }
            },
            onRename = {
                selectedApp?.let { viewModel.openRenameDialog(it) }
            },
            onAddToFolder = {
                selectedApp?.let { viewModel.openFolderPicker(it) }
            },
            onMoveUp = {
                selectedApp?.let { viewModel.moveFavoriteUp(it) }
            },
            onMoveDown = {
                selectedApp?.let { viewModel.moveFavoriteDown(it) }
            },
            onDismiss = { viewModel.closeAppActionMenu() }
        )

        // 9. Rename App Dialog
        RenameAppDialog(
            isOpen = state.isRenameDialogOpen,
            app = state.selectedAppForAction,
            onSave = { newName ->
                state.selectedAppForAction?.let { app ->
                    viewModel.saveCustomAppLabel(app, newName)
                }
            },
            onResetToOriginal = {
                state.selectedAppForAction?.let { app ->
                    viewModel.resetAppLabelToOriginal(app)
                }
            },
            onDismiss = { viewModel.closeRenameDialog() }
        )

        // 10. Folder Picker Sheet
        FolderPickerSheet(
            isOpen = state.isFolderPickerOpen,
            app = state.pendingAppForFolder,
            folders = state.folders,
            onSelectFolder = { folder ->
                state.pendingAppForFolder?.let { app ->
                    viewModel.addAppToFolder(folder, app)
                }
            },
            onCreateNewFolder = {
                viewModel.openCreateFolderDialog()
            },
            onDismiss = { viewModel.closeFolderPicker() }
        )

        // 11. Create / Rename Folder Dialog
        CreateFolderDialog(
            isOpen = state.isCreateFolderDialogOpen,
            initialName = "",
            title = "Create Folder",
            confirmButtonText = "Create",
            onConfirm = { name ->
                viewModel.createFolderWithName(name)
            },
            onDismiss = { viewModel.closeCreateFolderDialog() }
        )

        // 12. Folder View Sheet
        FolderViewSheet(
            isOpen = state.isFolderViewOpen,
            folder = state.selectedFolder,
            apps = state.allInstalledApps,
            preferences = state.preferences,
            onAppClick = { app ->
                viewModel.launchApp(app)
            },
            onAppLongClick = { app ->
                viewModel.openAppActionMenu(app, AppActionOrigin.HOME_SCREEN)
            },
            onRemoveAppFromFolder = { folder, app ->
                viewModel.removeAppFromFolder(folder, app)
            },
            onRenameFolder = { folder, newName ->
                viewModel.renameFolder(folder, newName)
            },
            onDeleteFolder = { folder ->
                viewModel.deleteFolder(folder)
            },
            onDismiss = { viewModel.closeFolderView() }
        )

        // 13. Recents Overview Sheet
        RecentsOverviewSheet(
            isOpen = state.isRecentsOpen,
            recentApps = state.recentApps,
            memoryInfo = state.memoryInfo,
            preferences = state.preferences,
            onAppSelect = { recentApp ->
                viewModel.launchRecentApp(recentApp)
            },
            onDismissApp = { recentApp ->
                viewModel.removeRecent(recentApp)
            },
            onClearAll = {
                viewModel.clearAllRecents()
            },
            onDismiss = {
                viewModel.closeRecents()
            }
        )

        // 14. NXV Lock Screen Overlay
        LockScreenOverlay(
            isLocked = state.isLocked,
            preferences = state.preferences,
            batteryInfo = state.batteryInfo,
            notifications = state.lockNotifications,
            isNotificationListenerGranted = state.isNotificationListenerGranted,
            isFlashlightOn = state.isFlashlightOn,
            currentTimeString = state.currentTimeString,
            currentDateString = state.currentDateString,
            onUnlock = { viewModel.unlockScreen() },
            onToggleFlashlight = { viewModel.toggleFlashlight() },
            onDismissNotification = { id -> viewModel.dismissLockNotification(id) },
            onTapNotification = { notif -> viewModel.onTapLockNotification(notif) },
            onRequestNotificationAccess = { viewModel.requestNotificationListenerAccess() }
        )
    }
}
