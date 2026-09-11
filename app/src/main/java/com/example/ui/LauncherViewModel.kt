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
import com.example.model.InstalledApp
import com.example.model.LauncherPreferences
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
    val isAppDrawerOpen: Boolean = false,
    val isSearchOpen: Boolean = false,
    val searchQuery: String = "",
    val filteredSearchApps: List<InstalledApp> = emptyList(),
    val isCustomizationOpen: Boolean = false,
    val preferences: LauncherPreferences = LauncherPreferences(),
    val selectedAppForAction: InstalledApp? = null,
    val isAppActionMenuOpen: Boolean = false,
    val isLoading: Boolean = true
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
    }

    fun updateDateTime() {
        val now = Calendar.getInstance().time
        val timeStr = timeFormat.format(now)
        val dateStr = dateFormat.format(now)
        _uiState.update {
            it.copy(
                currentTimeString = timeStr,
                currentDateString = dateStr
            )
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
            val apps = repository.getInstalledApps()
            val favoriteNames = repository.getFavoritePackageNames()

            val favMap = apps.associateBy { it.packageName }
            var favorites = favoriteNames.mapNotNull { favMap[it] }

            // If user has no favorites set yet (e.g. fresh install), take the first 8 available apps
            if (favorites.isEmpty() && apps.isNotEmpty()) {
                favorites = apps.take(8)
                repository.saveFavorites(favorites.map { it.packageName })
            }

            _uiState.update { current ->
                val filtered = if (current.searchQuery.isBlank()) {
                    apps
                } else {
                    val q = current.searchQuery.trim().lowercase()
                    apps.filter { it.label.lowercase().contains(q) }
                }
                current.copy(
                    allInstalledApps = apps,
                    favoriteApps = favorites,
                    filteredSearchApps = filtered,
                    isLoading = false
                )
            }
        }
    }

    fun launchApp(app: InstalledApp) {
        repository.launchApp(app)
        closeAllOverlays()
    }

    fun openAppDrawer() {
        _uiState.update {
            it.copy(
                isAppDrawerOpen = true,
                isSearchOpen = false,
                isCustomizationOpen = false,
                isAppActionMenuOpen = false
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
                current.allInstalledApps.filter { it.label.lowercase().contains(q) }
            }
            current.copy(
                searchQuery = query,
                filteredSearchApps = filtered
            )
        }
    }

    fun openCustomization() {
        _uiState.update {
            it.copy(
                isCustomizationOpen = true,
                isAppDrawerOpen = false,
                isSearchOpen = false,
                isAppActionMenuOpen = false
            )
        }
    }

    fun closeCustomization() {
        _uiState.update { it.copy(isCustomizationOpen = false) }
    }

    fun openAppActionMenu(app: InstalledApp) {
        _uiState.update {
            it.copy(
                selectedAppForAction = app,
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
        repository.addFavorite(app.packageName)
        reloadApps()
    }

    fun removeFavorite(app: InstalledApp) {
        repository.removeFavorite(app.packageName)
        reloadApps()
        closeAppActionMenu()
    }

    fun moveFavoriteUp(app: InstalledApp) {
        val current = _uiState.value.favoriteApps
        val idx = current.indexOfFirst { it.packageName == app.packageName }
        if (idx > 0) {
            repository.moveFavorite(idx, idx - 1)
            reloadApps()
        }
    }

    fun moveFavoriteDown(app: InstalledApp) {
        val current = _uiState.value.favoriteApps
        val idx = current.indexOfFirst { it.packageName == app.packageName }
        if (idx >= 0 && idx < current.size - 1) {
            repository.moveFavorite(idx, idx + 1)
            reloadApps()
        }
    }

    fun updatePreferences(newPrefs: LauncherPreferences) {
        repository.savePreferences(newPrefs)
        _uiState.update { it.copy(preferences = newPrefs) }
    }

    private fun loadPreferences() {
        val prefs = repository.getPreferences()
        _uiState.update { it.copy(preferences = prefs) }
    }

    fun closeAllOverlays() {
        _uiState.update {
            it.copy(
                isAppDrawerOpen = false,
                isSearchOpen = false,
                isCustomizationOpen = false,
                isAppActionMenuOpen = false,
                selectedAppForAction = null
            )
        }
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
