package com.example.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LauncherRepository
import com.example.model.AppActionOrigin
import com.example.model.AppFolder
import com.example.model.AppSortOrder
import com.example.model.HomeItem
import com.example.model.HomeScreenMode
import com.example.model.InstalledApp
import com.example.model.LauncherPreferences
import com.example.model.MemoryStatusInfo
import com.example.model.RecentAppItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class LauncherUiState(
    val currentTimeString: String = "12:00",
    val currentDateString: String = "Mon, Jan 1",
    val weatherTempString: String = "23°",
    val weatherCondition: String = "Cloudy",
    val allInstalledApps: List<InstalledApp> = emptyList(),
    val favoriteApps: List<InstalledApp> = emptyList(),
    val folders: List<AppFolder> = emptyList(),
    val homeItems: List<HomeItem> = emptyList(),
    val recentApps: List<RecentAppItem> = emptyList(),
    val memoryInfo: MemoryStatusInfo = MemoryStatusInfo(),
    val isAppDrawerOpen: Boolean = false,
    val isSearchOpen: Boolean = false,
    val isRecentsOpen: Boolean = false,
    val searchQuery: String = "",
    val filteredSearchApps: List<InstalledApp> = emptyList(),
    val isCustomizationOpen: Boolean = false,
    val preferences: LauncherPreferences = LauncherPreferences(),
    val selectedAppForAction: InstalledApp? = null,
    val appActionOrigin: AppActionOrigin = AppActionOrigin.HOME_SCREEN,
    val isAppActionMenuOpen: Boolean = false,
    val isRenameDialogOpen: Boolean = false,
    val isCreateFolderDialogOpen: Boolean = false,
    val isFolderPickerOpen: Boolean = false,
    val isFolderViewOpen: Boolean = false,
    val isQuickHomeOptionsOpen: Boolean = false,
    val selectedFolder: AppFolder? = null,
    val pendingAppForFolder: InstalledApp? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 2,
    val isLoading: Boolean = true,
    // LOCK SCREEN STATE
    val isLocked: Boolean = false,
    val isFlashlightOn: Boolean = false,
    val batteryInfo: com.example.model.BatteryStatusInfo = com.example.model.BatteryStatusInfo(),
    val lockNotifications: List<com.example.model.LockNotificationItem> = emptyList(),
    val isNotificationListenerGranted: Boolean = false
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LauncherRepository(application)

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    // Receiver to detect when apps are installed, uninstalled or updated in real-time
    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            reloadApps()
        }
    }

    init {
        loadPreferences()
        updateDateTime()
        registerPackageReceiver()
        reloadApps()
        refreshLockScreenData()
        observeNotificationListenerService()
    }

    fun lockScreen() {
        if (_uiState.value.preferences.lockScreenEnabled) {
            refreshLockScreenData()
            closeAllOverlays()
            _uiState.update { it.copy(isLocked = true) }
        }
    }

    fun unlockScreen() {
        _uiState.update { it.copy(isLocked = false) }
    }

    fun toggleFlashlight() {
        val newState = repository.toggleFlashlight()
        _uiState.update { it.copy(isFlashlightOn = newState) }
    }

    fun requestNotificationListenerAccess() {
        repository.openNotificationListenerSettings()
    }

    fun refreshLockScreenData() {
        val battery = repository.getBatteryStatus()
        val isGranted = repository.isNotificationListenerAccessGranted()
        _uiState.update {
            it.copy(
                batteryInfo = battery,
                isNotificationListenerGranted = isGranted,
                lockNotifications = if (it.lockNotifications.isEmpty()) getDefaultSampleNotifications() else it.lockNotifications
            )
        }
    }

    fun dismissLockNotification(id: String) {
        val service = com.example.service.NXVNotificationListenerService.instance
        service?.removeNotificationByKey(id)
        _uiState.update { current ->
            current.copy(lockNotifications = current.lockNotifications.filterNot { it.id == id })
        }
    }

    fun onTapLockNotification(notif: com.example.model.LockNotificationItem) {
        unlockScreen()
        val app = _uiState.value.allInstalledApps.firstOrNull { it.packageName == notif.packageName }
        if (app != null) {
            launchApp(app)
        }
    }

    private fun observeNotificationListenerService() {
        viewModelScope.launch {
            com.example.service.NXVNotificationListenerService.activeSbnList.collect { sbnList ->
                val isGranted = repository.isNotificationListenerAccessGranted()
                if (sbnList.isNotEmpty()) {
                    val mapped = sbnList.mapNotNull { sbn ->
                        val pkg = sbn.packageName ?: return@mapNotNull null
                        val extras = sbn.notification?.extras ?: return@mapNotNull null
                        val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: ""
                        val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""
                        if (title.isBlank() && text.isBlank()) return@mapNotNull null

                        val app = _uiState.value.allInstalledApps.firstOrNull { it.packageName == pkg }
                        val appName = app?.displayName ?: pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }

                        com.example.model.LockNotificationItem(
                            id = sbn.key ?: "${pkg}_${sbn.postTime}",
                            packageName = pkg,
                            appName = appName,
                            title = title,
                            text = text,
                            timestamp = sbn.postTime,
                            timeFormatted = "Just now",
                            appIconDrawable = app?.icon,
                            isSensitive = sbn.notification?.visibility == android.app.Notification.VISIBILITY_PRIVATE
                        )
                    }
                    _uiState.update {
                        it.copy(
                            isNotificationListenerGranted = isGranted,
                            lockNotifications = mapped.ifEmpty { getDefaultSampleNotifications() }
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isNotificationListenerGranted = isGranted,
                            lockNotifications = if (it.lockNotifications.isEmpty()) getDefaultSampleNotifications() else it.lockNotifications
                        )
                    }
                }
            }
        }
    }

    private fun getDefaultSampleNotifications(): List<com.example.model.LockNotificationItem> {
        return listOf(
            com.example.model.LockNotificationItem(
                id = "notif_sample_1",
                packageName = "com.google.android.apps.messaging",
                appName = "Messages",
                title = "Alex Vance",
                text = "Are we still meeting at 5 for the NXV build test?",
                timeFormatted = "2m ago",
                isSensitive = false
            ),
            com.example.model.LockNotificationItem(
                id = "notif_sample_2",
                packageName = "com.google.android.gm",
                appName = "Gmail",
                title = "Google AI Studio",
                text = "Your application build is completed and live.",
                timeFormatted = "15m ago",
                isSensitive = true
            )
        )
    }

    fun updateDateTime() {
        val now = Calendar.getInstance().time
        val timeStr = timeFormat.format(now)
        val dateStr = dateFormat.format(now)
        _uiState.update { current ->
            if (current.currentTimeString == timeStr && current.currentDateString == dateStr) {
                current
            } else {
                current.copy(
                    currentTimeString = timeStr,
                    currentDateString = dateStr
                )
            }
        }
    }

    private fun registerPackageReceiver() {
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getApplication<Application>().registerReceiver(
                    packageReceiver,
                    filter,
                    Context.RECEIVER_EXPORTED
                )
            } else {
                getApplication<Application>().registerReceiver(packageReceiver, filter)
            }
        } catch (e: Exception) {
            // Receiver registration safety fallback
        }
    }

    fun reloadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val sortOrder = _uiState.value.preferences.appSortOrder
            val apps = repository.getInstalledApps(sortOrder)
            val favoriteNames = repository.getFavoritePackageNames()
            val savedFolders = repository.getFolders()

            val favMap = apps.associateBy { it.packageName }
            val fillCells = _uiState.value.preferences.fillCellsOfUninstalledApps

            var favorites = if (fillCells) {
                favoriteNames.mapNotNull { favMap[it] }
            } else {
                val validFavs = favoriteNames.filter { favMap.containsKey(it) }
                if (validFavs.size != favoriteNames.size) {
                    repository.saveFavorites(validFavs)
                }
                validFavs.mapNotNull { favMap[it] }
            }

            // Fallback initial favorites
            if (favorites.isEmpty() && apps.isNotEmpty()) {
                favorites = apps.take(16)
                repository.saveFavorites(favorites.map { it.packageName })
            }

            // Construct HomeItems (Folders + Individual Favorite Apps)
            val homeItemsList = mutableListOf<HomeItem>()
            val folderPackageSet = savedFolders.flatMap { it.packageNames }.toSet()

            savedFolders.forEach { folder ->
                val folderApps = folder.packageNames.mapNotNull { favMap[it] }
                if (folderApps.isNotEmpty()) {
                    homeItemsList.add(HomeItem.Folder(folder, folderApps))
                }
            }

            favorites.forEach { app ->
                if (!folderPackageSet.contains(app.packageName)) {
                    homeItemsList.add(HomeItem.App(app))
                }
            }

            val recentsList = repository.getRecentAppsList(apps)
            val memInfo = repository.getSystemMemoryInfo()

            _uiState.update { current ->
                val filtered = if (current.searchQuery.isBlank()) {
                    apps
                } else {
                    val q = current.searchQuery.trim().lowercase()
                    apps.filter { it.displayName.lowercase().contains(q) || it.label.lowercase().contains(q) }
                }

                val currentSelectedFolder = current.selectedFolder?.let { sel ->
                    savedFolders.firstOrNull { it.id == sel.id }
                }

                current.copy(
                    allInstalledApps = apps,
                    favoriteApps = favorites,
                    folders = savedFolders,
                    homeItems = homeItemsList,
                    recentApps = recentsList,
                    memoryInfo = memInfo,
                    filteredSearchApps = filtered,
                    selectedFolder = currentSelectedFolder,
                    isLoading = false
                )
            }
        }
    }

    fun launchApp(app: InstalledApp) {
        val success = repository.launchApp(app)
        if (success) {
            repository.recordRecentApp(app.packageName)
            closeAllOverlays()
            viewModelScope.launch {
                val recents = repository.getRecentAppsList(_uiState.value.allInstalledApps)
                val mem = repository.getSystemMemoryInfo()
                _uiState.update { it.copy(recentApps = recents, memoryInfo = mem) }
            }
        } else {
            try {
                android.widget.Toast.makeText(
                    getApplication(),
                    "Unable to launch ${app.displayName}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                // Ignore toast failure
            }
            reloadApps()
        }
    }

    fun openAppInfo(app: InstalledApp) {
        repository.openAppInfo(app.packageName)
        closeAppActionMenu()
    }

    fun uninstallApp(app: InstalledApp) {
        repository.uninstallApp(app.packageName)
        closeAppActionMenu()
    }

    fun launchRecentApp(recent: RecentAppItem) {
        val app = _uiState.value.allInstalledApps.firstOrNull { it.packageName == recent.packageName }
        if (app != null) {
            launchApp(app)
        } else {
            val intent = getApplication<Application>().packageManager.getLaunchIntentForPackage(recent.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                getApplication<Application>().startActivity(intent)
            }
            closeAllOverlays()
        }
    }

    fun openRecents() {
        viewModelScope.launch {
            val recents = repository.getRecentAppsList(_uiState.value.allInstalledApps)
            val mem = repository.getSystemMemoryInfo()
            _uiState.update {
                it.copy(
                    isRecentsOpen = true,
                    isAppDrawerOpen = false,
                    isSearchOpen = false,
                    isCustomizationOpen = false,
                    isAppActionMenuOpen = false,
                    recentApps = recents,
                    memoryInfo = mem
                )
            }
        }
    }

    fun closeRecents() {
        _uiState.update { it.copy(isRecentsOpen = false) }
    }

    fun removeRecent(recentApp: RecentAppItem) {
        repository.removeRecentApp(recentApp.packageName)
        viewModelScope.launch {
            val recents = repository.getRecentAppsList(_uiState.value.allInstalledApps)
            val mem = repository.getSystemMemoryInfo()
            _uiState.update { it.copy(recentApps = recents, memoryInfo = mem) }
        }
    }

    fun clearAllRecents() {
        repository.clearAllRecentApps()
        viewModelScope.launch {
            val mem = repository.getSystemMemoryInfo()
            _uiState.update { it.copy(recentApps = emptyList(), memoryInfo = mem) }
        }
    }

    fun refreshMemoryInfo() {
        val mem = repository.getSystemMemoryInfo()
        _uiState.update { it.copy(memoryInfo = mem) }
    }

    fun openAppDrawer() {
        _uiState.update {
            it.copy(
                isAppDrawerOpen = true,
                isSearchOpen = false,
                isCustomizationOpen = false,
                isAppActionMenuOpen = false,
                isRecentsOpen = false,
                isQuickHomeOptionsOpen = false
            )
        }
    }

    fun closeAppDrawer() {
        _uiState.update { it.copy(isAppDrawerOpen = false) }
    }

    fun openSearch() {
        _uiState.update {
            it.copy(
                isSearchOpen = true,
                isAppDrawerOpen = false,
                isCustomizationOpen = false,
                isRecentsOpen = false,
                isQuickHomeOptionsOpen = false,
                searchQuery = "",
                filteredSearchApps = it.allInstalledApps
            )
        }
    }

    fun closeSearch() {
        _uiState.update {
            it.copy(
                isSearchOpen = false,
                searchQuery = "",
                filteredSearchApps = it.allInstalledApps
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val filtered = if (query.isBlank()) {
                current.allInstalledApps
            } else {
                val q = query.trim().lowercase()
                current.allInstalledApps.filter {
                    it.displayName.lowercase().contains(q) ||
                    it.label.lowercase().contains(q) ||
                    it.packageName.lowercase().contains(q)
                }
            }
            current.copy(
                searchQuery = query,
                filteredSearchApps = filtered
            )
        }
    }

    fun setAppSortOrder(order: AppSortOrder) {
        val newPrefs = _uiState.value.preferences.copy(appSortOrder = order)
        updatePreferences(newPrefs)
        reloadApps()
    }

    fun openCustomization() {
        _uiState.update {
            it.copy(
                isCustomizationOpen = true,
                isAppDrawerOpen = false,
                isSearchOpen = false,
                isAppActionMenuOpen = false,
                isRecentsOpen = false,
                isQuickHomeOptionsOpen = false
            )
        }
    }

    fun closeCustomization() {
        _uiState.update { it.copy(isCustomizationOpen = false) }
    }

    fun openQuickHomeOptions() {
        _uiState.update {
            it.copy(
                isQuickHomeOptionsOpen = true,
                isAppDrawerOpen = false,
                isSearchOpen = false,
                isCustomizationOpen = false,
                isAppActionMenuOpen = false,
                isRecentsOpen = false
            )
        }
    }

    fun closeQuickHomeOptions() {
        _uiState.update { it.copy(isQuickHomeOptionsOpen = false) }
    }

    fun toggleHomeScreenMode() {
        val currentMode = _uiState.value.preferences.homeScreenMode
        val nextMode = if (currentMode == HomeScreenMode.GRID) HomeScreenMode.VERTICAL_DOCK else HomeScreenMode.GRID
        val newPrefs = _uiState.value.preferences.copy(homeScreenMode = nextMode)
        updatePreferences(newPrefs)
    }

    fun toggleLockHomeScreenLayout() {
        val newLocked = !_uiState.value.preferences.lockHomeScreenLayout
        val newPrefs = _uiState.value.preferences.copy(lockHomeScreenLayout = newLocked)
        updatePreferences(newPrefs)
    }

    fun openAppActionMenu(app: InstalledApp, origin: AppActionOrigin = AppActionOrigin.HOME_SCREEN) {
        _uiState.update {
            it.copy(
                selectedAppForAction = app,
                appActionOrigin = origin,
                isAppActionMenuOpen = true
            )
        }
    }

    fun closeAppActionMenu() {
        _uiState.update {
            it.copy(
                selectedAppForAction = null,
                isAppActionMenuOpen = false
            )
        }
    }

    fun addFavorite(app: InstalledApp) {
        if (_uiState.value.preferences.lockHomeScreenLayout) return
        repository.addFavorite(app.packageName)
        reloadApps()
        closeAppActionMenu()
    }

    fun removeFavorite(app: InstalledApp) {
        if (_uiState.value.preferences.lockHomeScreenLayout) return
        repository.removeFavorite(app.packageName)
        reloadApps()
        closeAppActionMenu()
    }

    fun moveFavoriteUp(app: InstalledApp) {
        if (_uiState.value.preferences.lockHomeScreenLayout) return
        val current = _uiState.value.favoriteApps
        val idx = current.indexOfFirst { it.packageName == app.packageName }
        if (idx > 0) {
            repository.moveFavorite(idx, idx - 1)
            reloadApps()
        }
    }

    fun moveFavoriteDown(app: InstalledApp) {
        if (_uiState.value.preferences.lockHomeScreenLayout) return
        val current = _uiState.value.favoriteApps
        val idx = current.indexOfFirst { it.packageName == app.packageName }
        if (idx >= 0 && idx < current.size - 1) {
            repository.moveFavorite(idx, idx + 1)
            reloadApps()
        }
    }

    fun reorderFavoriteApps(fromIndex: Int, toIndex: Int) {
        if (_uiState.value.preferences.lockHomeScreenLayout) return
        repository.moveFavorite(fromIndex, toIndex)
        reloadApps()
    }

    // Rename App Actions
    fun openRenameDialog(app: InstalledApp) {
        _uiState.update {
            it.copy(
                selectedAppForAction = app,
                isRenameDialogOpen = true,
                isAppActionMenuOpen = false
            )
        }
    }

    fun closeRenameDialog() {
        _uiState.update { it.copy(isRenameDialogOpen = false) }
    }

    fun saveCustomAppLabel(app: InstalledApp, customName: String) {
        repository.saveCustomAppLabel(app.packageName, customName)
        reloadApps()
        closeRenameDialog()
    }

    fun resetAppLabelToOriginal(app: InstalledApp) {
        repository.removeCustomAppLabel(app.packageName)
        reloadApps()
        closeRenameDialog()
    }

    // Folder Actions
    fun openFolderPicker(app: InstalledApp) {
        _uiState.update {
            it.copy(
                pendingAppForFolder = app,
                isFolderPickerOpen = true,
                isAppActionMenuOpen = false
            )
        }
    }

    fun closeFolderPicker() {
        _uiState.update {
            it.copy(
                isFolderPickerOpen = false,
                pendingAppForFolder = null
            )
        }
    }

    fun openCreateFolderDialog(initialApp: InstalledApp? = null) {
        _uiState.update {
            it.copy(
                pendingAppForFolder = initialApp ?: it.selectedAppForAction ?: it.pendingAppForFolder,
                isCreateFolderDialogOpen = true,
                isFolderPickerOpen = false,
                isAppActionMenuOpen = false
            )
        }
    }

    fun closeCreateFolderDialog() {
        _uiState.update {
            it.copy(
                isCreateFolderDialogOpen = false,
                pendingAppForFolder = null
            )
        }
    }

    fun createFolderWithName(name: String) {
        val app = _uiState.value.pendingAppForFolder
        val pkgs = if (app != null) listOf(app.packageName) else emptyList()
        repository.createFolder(name, pkgs)
        reloadApps()
        closeCreateFolderDialog()
    }

    fun mergeAppsIntoFolder(draggedApp: InstalledApp, targetApp: InstalledApp) {
        if (_uiState.value.preferences.lockHomeScreenLayout) return
        if (draggedApp.packageName == targetApp.packageName) return

        val folderName = "${targetApp.displayName} & more"
        repository.createFolder(folderName, listOf(targetApp.packageName, draggedApp.packageName))
        reloadApps()
    }

    fun addAppToFolder(folder: AppFolder, app: InstalledApp) {
        repository.addAppToFolder(folder.id, app.packageName)
        reloadApps()
        closeFolderPicker()
    }

    fun removeAppFromFolder(folder: AppFolder, app: InstalledApp) {
        repository.removeAppFromFolder(folder.id, app.packageName)
        reloadApps()
    }

    fun renameFolder(folder: AppFolder, newName: String) {
        repository.renameFolder(folder.id, newName)
        reloadApps()
    }

    fun deleteFolder(folder: AppFolder) {
        repository.deleteFolder(folder.id)
        reloadApps()
        closeFolderView()
    }

    fun openFolderView(folder: AppFolder) {
        _uiState.update {
            it.copy(
                selectedFolder = folder,
                isFolderViewOpen = true,
                isAppDrawerOpen = false,
                isSearchOpen = false,
                isCustomizationOpen = false,
                isAppActionMenuOpen = false,
                isRecentsOpen = false,
                isQuickHomeOptionsOpen = false
            )
        }
    }

    fun closeFolderView() {
        _uiState.update {
            it.copy(
                isFolderViewOpen = false,
                selectedFolder = null
            )
        }
    }

    fun updatePreferences(newPrefs: LauncherPreferences) {
        val oldPrefs = _uiState.value.preferences
        val wallpaperChanged = oldPrefs.selectedWallpaperId != newPrefs.selectedWallpaperId
        repository.savePreferences(newPrefs)
        _uiState.update { it.copy(preferences = newPrefs) }

        if (wallpaperChanged && newPrefs.autoApplySystemWallpaper) {
            viewModelScope.launch {
                repository.applyWallpaperToSystem(newPrefs.selectedWallpaper)
            }
        }
    }

    fun applySystemWallpaper(wallpaper: com.example.model.LauncherWallpaper, lockScreenToo: Boolean = true) {
        viewModelScope.launch {
            repository.applyWallpaperToSystem(wallpaper, lockScreenToo)
        }
    }

    private fun loadPreferences() {
        val prefs = repository.getPreferences()
        _uiState.update { it.copy(preferences = prefs) }
        if (prefs.autoApplySystemWallpaper) {
            viewModelScope.launch {
                repository.applyWallpaperToSystem(prefs.selectedWallpaper)
            }
        }
    }

    fun closeAllOverlays() {
        _uiState.update {
            it.copy(
                isAppDrawerOpen = false,
                isSearchOpen = false,
                isCustomizationOpen = false,
                isAppActionMenuOpen = false,
                isRecentsOpen = false,
                isQuickHomeOptionsOpen = false,
                isFolderViewOpen = false,
                isFolderPickerOpen = false,
                isRenameDialogOpen = false,
                isCreateFolderDialogOpen = false,
                selectedAppForAction = null,
                currentPage = 0
            )
        }
    }

    fun nextPage() {
        _uiState.update {
            if (it.currentPage < it.totalPages - 1) it.copy(currentPage = it.currentPage + 1) else it
        }
    }

    fun prevPage() {
        _uiState.update {
            if (it.currentPage > 0) it.copy(currentPage = it.currentPage - 1) else it
        }
    }

    fun setPage(page: Int) {
        _uiState.update { it.copy(currentPage = page.coerceIn(0, it.totalPages - 1)) }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(packageReceiver)
        } catch (e: Exception) {
            // Unregister safety
        }
    }
}
