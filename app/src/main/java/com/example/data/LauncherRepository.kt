package com.example.data

import android.app.ActivityManager
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.model.AppFolder
import com.example.model.AppSortOrder
import com.example.model.ClockPosition
import com.example.model.HomeGridLayout
import com.example.model.HomeScreenMode
import com.example.model.InstalledApp
import com.example.model.LauncherPreferences
import com.example.model.LauncherWallpaper
import com.example.model.MemoryStatusInfo
import com.example.model.RecentAppItem
import com.example.model.RecentsOrientation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Collections
import java.util.UUID

class LauncherRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nxv_launcher_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FAVORITES = "favorite_package_names"
        private const val KEY_RECENT_PACKAGES = "recent_launched_package_names"
        private const val KEY_HOME_SCREEN_MODE = "pref_home_screen_mode"
        private const val KEY_HOME_GRID_LAYOUT = "pref_home_grid_layout"
        private const val KEY_FILL_UNINSTALLED_CELLS = "pref_fill_uninstalled_cells"
        private const val KEY_LOCK_HOME_LAYOUT = "pref_lock_home_layout"
        private const val KEY_RECENTS_ORIENTATION = "pref_recents_orientation"
        private const val KEY_SHOW_MEMORY_STATUS = "pref_show_memory_status"
        private const val KEY_BLUR_APP_PREVIEWS = "pref_blur_app_previews"
        private const val KEY_ICON_SIZE = "pref_icon_size"
        private const val KEY_SHOW_NAMES = "pref_show_names"
        private const val KEY_ITEM_SPACING = "pref_item_spacing"
        private const val KEY_CLOCK_SIZE = "pref_clock_size"
        private const val KEY_CLOCK_POS = "pref_clock_pos"
        private const val KEY_CLOCK_FONT = "pref_clock_font"
        private const val KEY_DIM_BG = "pref_dim_bg"
        private const val KEY_LEFT_SCRIM = "pref_left_scrim"
        private const val KEY_SHOW_WEATHER = "pref_show_weather"
        private const val KEY_SHOW_ALL_APPS = "pref_show_all_apps"
        private const val KEY_ENABLE_SOUND = "pref_enable_sound"
        private const val KEY_ENABLE_HAPTIC = "pref_enable_haptic"
        private const val KEY_AUTO_APPLY_WALLPAPER = "pref_auto_apply_wallpaper"
        private const val KEY_THEMED_ICONS = "pref_themed_icons"
        private const val KEY_THEMED_ICON_COLOR = "pref_themed_icon_color"
        private const val KEY_THEMED_ICON_COLOR_MODE = "pref_themed_icon_color_mode"
        private const val KEY_CUSTOM_THEMED_COLOR_HEX = "pref_custom_themed_color_hex"
        private const val KEY_THEMED_ICON_STYLE = "pref_themed_icon_style"
        private const val KEY_FISHEYE_SCROLL = "pref_fisheye_scroll"
        private const val KEY_FISHEYE_MAG = "pref_fisheye_mag"
        private const val KEY_WALLPAPER_ID = "pref_wallpaper_id"
        private const val KEY_APP_SORT_ORDER = "pref_app_sort_order"
        private const val KEY_CUSTOM_APP_LABELS = "custom_app_labels_json"
        private const val KEY_APP_FOLDERS = "app_folders_json"
        private const val KEY_LAUNCH_COUNTS = "app_launch_counts_json"
        private const val KEY_LAST_LAUNCH_TIMES = "app_last_launch_times_json"
        private const val KEY_HOME_ITEMS_ORDER = "home_items_order"

        // LOCK SCREEN PREF KEYS
        private const val KEY_LOCK_ENABLED = "pref_lock_enabled"
        private const val KEY_LOCK_CLOCK_24H = "pref_lock_clock_24h"
        private const val KEY_LOCK_CLOCK_FONT = "pref_lock_clock_font"
        private const val KEY_LOCK_CLOCK_SIZE = "pref_lock_clock_size"
        private const val KEY_LOCK_CLOCK_POS = "pref_lock_clock_pos"
        private const val KEY_LOCK_SHOW_DATE = "pref_lock_show_date"
        private const val KEY_LOCK_SHOW_SECONDS = "pref_lock_show_seconds"
        private const val KEY_LOCK_SHOW_WEATHER = "pref_lock_show_weather"
        private const val KEY_LOCK_SHOW_NOTIFS = "pref_lock_show_notifs"
        private const val KEY_LOCK_SHOW_NOTIF_CONTENT = "pref_lock_show_notif_content"
        private const val KEY_LOCK_HIDE_SENSITIVE = "pref_lock_hide_sensitive"
        private const val KEY_LOCK_SHOW_NOTIF_COUNT = "pref_lock_show_notif_count"
        private const val KEY_LOCK_LEFT_SHORTCUT = "pref_lock_left_shortcut"
        private const val KEY_LOCK_RIGHT_SHORTCUT = "pref_lock_right_shortcut"
        private const val KEY_LOCK_DIM = "pref_lock_dim"
        private const val KEY_LOCK_ENABLE_ANIM = "pref_lock_enable_anim"
        private const val KEY_LOCK_ANIM_SPEED = "pref_lock_anim_speed"

        private const val TAG = "NXVLauncherRepo"
        const val EMPTY_CELL_PLACEHOLDER = "__EMPTY_CELL_SLOT__"
    }

    // Cache icons in memory to avoid repeated heavy binder calls to PackageManager
    private val iconCache = Collections.synchronizedMap(mutableMapOf<String, Drawable>())

    suspend fun getInstalledApps(sortOrder: AppSortOrder = AppSortOrder.ALPHABETICAL): List<InstalledApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }

        val myPackageName = context.packageName
        val customLabels = getCustomAppLabels()
        val launchCounts = getAppLaunchCounts()
        val lastLaunchTimes = getAppLastLaunchTimes()

        val apps = resolveInfos
            .filter { resolveInfo ->
                // Don't show our own launcher in the app drawer list
                val pkg = resolveInfo.activityInfo.packageName
                pkg != myPackageName
            }
            .map { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val activityName = resolveInfo.activityInfo.name
                val label = try {
                    resolveInfo.loadLabel(pm).toString()
                } catch (e: Exception) {
                    packageName
                }

                val icon: Drawable? = iconCache.getOrPut(packageName) {
                    try {
                        resolveInfo.loadIcon(pm)
                    } catch (e: Exception) {
                        pm.defaultActivityIcon
                    }
                }

                val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and
                        android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0

                InstalledApp(
                    packageName = packageName,
                    activityName = activityName,
                    label = label,
                    customLabel = customLabels[packageName],
                    icon = icon,
                    isSystemApp = isSystem,
                    launchCount = launchCounts[packageName] ?: 0,
                    lastLaunchTime = lastLaunchTimes[packageName] ?: 0L
                )
            }
            .distinctBy { it.packageName }

        val currentInstalledPackages = apps.map { it.packageName }.toSet()
        iconCache.keys.retainAll(currentInstalledPackages)

        when (sortOrder) {
            AppSortOrder.ALPHABETICAL -> apps.sortedBy { it.displayName.lowercase() }
            AppSortOrder.RECENTLY_USED -> apps.sortedWith(
                compareByDescending<InstalledApp> { it.lastLaunchTime }
                    .thenBy { it.displayName.lowercase() }
            )
            AppSortOrder.MOST_USED -> apps.sortedWith(
                compareByDescending<InstalledApp> { it.launchCount }
                    .thenBy { it.displayName.lowercase() }
            )
        }
    }

    fun launchApp(app: InstalledApp): Boolean {
        recordAppLaunch(app.packageName)
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName(app.packageName, app.activityName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed launching component: ${app.packageName}/${app.activityName}", e)
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    true
                } else {
                    false
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback launch failed for ${app.packageName}", e2)
                false
            }
        }
    }

    fun openAppInfo(packageName: String): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open App Info for $packageName", e)
            false
        }
    }

    fun uninstallApp(packageName: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request uninstall for $packageName", e)
            false
        }
    }

    fun getCustomAppLabels(): Map<String, String> {
        val raw = prefs.getString(KEY_CUSTOM_APP_LABELS, null) ?: return emptyMap()
        val result = mutableMapOf<String, String>()
        try {
            val json = JSONObject(raw)
            val keys = json.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                result[k] = json.optString(k)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing custom app labels", e)
        }
        return result
    }

    fun saveCustomAppLabel(packageName: String, customLabel: String) {
        val current = getCustomAppLabels().toMutableMap()
        if (customLabel.isBlank()) {
            current.remove(packageName)
        } else {
            current[packageName] = customLabel.trim()
        }
        val json = JSONObject(current as Map<*, *>)
        prefs.edit().putString(KEY_CUSTOM_APP_LABELS, json.toString()).apply()
    }

    fun removeCustomAppLabel(packageName: String) {
        saveCustomAppLabel(packageName, "")
    }

    fun getAppLaunchCounts(): Map<String, Int> {
        val raw = prefs.getString(KEY_LAUNCH_COUNTS, null) ?: return emptyMap()
        val result = mutableMapOf<String, Int>()
        try {
            val json = JSONObject(raw)
            val keys = json.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                result[k] = json.optInt(k, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing launch counts", e)
        }
        return result
    }

    fun getAppLastLaunchTimes(): Map<String, Long> {
        val raw = prefs.getString(KEY_LAST_LAUNCH_TIMES, null) ?: return emptyMap()
        val result = mutableMapOf<String, Long>()
        try {
            val json = JSONObject(raw)
            val keys = json.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                result[k] = json.optLong(k, 0L)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing last launch times", e)
        }
        return result
    }

    fun recordAppLaunch(packageName: String) {
        val now = System.currentTimeMillis()
        val counts = getAppLaunchCounts().toMutableMap()
        counts[packageName] = (counts[packageName] ?: 0) + 1

        val times = getAppLastLaunchTimes().toMutableMap()
        times[packageName] = now

        prefs.edit()
            .putString(KEY_LAUNCH_COUNTS, JSONObject(counts as Map<*, *>).toString())
            .putString(KEY_LAST_LAUNCH_TIMES, JSONObject(times as Map<*, *>).toString())
            .apply()
    }

    fun getFolders(): List<AppFolder> {
        val raw = prefs.getString(KEY_APP_FOLDERS, null) ?: return emptyList()
        val result = mutableListOf<AppFolder>()
        try {
            val jsonArr = JSONArray(raw)
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                val id = obj.optString("id", UUID.randomUUID().toString())
                val name = obj.optString("name", "Folder")
                val pkgsArr = obj.optJSONArray("packageNames") ?: JSONArray()
                val pkgs = mutableListOf<String>()
                for (j in 0 until pkgsArr.length()) {
                    pkgs.add(pkgsArr.getString(j))
                }
                result.add(AppFolder(id = id, name = name, packageNames = pkgs))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing folders", e)
        }
        return result
    }

    fun saveFolders(folders: List<AppFolder>) {
        val jsonArr = JSONArray()
        folders.forEach { folder ->
            val obj = JSONObject().apply {
                put("id", folder.id)
                put("name", folder.name)
                val pkgsArr = JSONArray()
                folder.packageNames.forEach { pkgsArr.put(it) }
                put("packageNames", pkgsArr)
            }
            jsonArr.put(obj)
        }
        prefs.edit().putString(KEY_APP_FOLDERS, jsonArr.toString()).apply()
    }

    fun createFolder(name: String, packageNames: List<String>): AppFolder {
        val newFolder = AppFolder(
            id = UUID.randomUUID().toString(),
            name = if (name.isBlank()) "Folder" else name.trim(),
            packageNames = packageNames.distinct()
        )
        val current = getFolders().toMutableList()
        current.add(newFolder)
        saveFolders(current)
        return newFolder
    }

    fun renameFolder(folderId: String, newName: String) {
        val current = getFolders().map {
            if (it.id == folderId) it.copy(name = if (newName.isBlank()) "Folder" else newName.trim()) else it
        }
        saveFolders(current)
    }

    fun deleteFolder(folderId: String) {
        val current = getFolders().filterNot { it.id == folderId }
        saveFolders(current)
    }

    fun addAppToFolder(folderId: String, packageName: String) {
        val current = getFolders().map { folder ->
            if (folder.id == folderId) {
                val pkgs = folder.packageNames.toMutableList()
                if (!pkgs.contains(packageName)) pkgs.add(packageName)
                folder.copy(packageNames = pkgs)
            } else folder
        }
        saveFolders(current)
    }

    fun removeAppFromFolder(folderId: String, packageName: String) {
        val current = getFolders().mapNotNull { folder ->
            if (folder.id == folderId) {
                val pkgs = folder.packageNames.filterNot { it == packageName }
                if (pkgs.isEmpty()) null else folder.copy(packageNames = pkgs)
            } else folder
        }
        saveFolders(current)
    }

    fun getFavoritePackageNames(): List<String> {
        val raw = prefs.getString(KEY_FAVORITES, null)
        return if (raw.isNullOrBlank()) {
            // Default smart favorite recommendations commonly found on Android
            listOf(
                "com.google.android.apps.messaging",
                "com.google.android.dialer",
                "com.google.android.apps.photos",
                "com.android.chrome",
                "com.google.android.youtube",
                "com.google.android.gm",
                "com.whatsapp",
                "com.instagram.android",
                "com.twitter.android"
            )
        } else {
            raw.split(",").filter { it.isNotBlank() }
        }
    }

    fun saveFavorites(packageNames: List<String>) {
        val joined = packageNames.joinToString(",")
        prefs.edit().putString(KEY_FAVORITES, joined).apply()
    }

    fun addFavorite(packageName: String) {
        val current = getFavoritePackageNames().toMutableList()
        if (!current.contains(packageName)) {
            current.add(packageName)
            saveFavorites(current)
        }
    }

    fun removeFavorite(packageName: String) {
        val current = getFavoritePackageNames().toMutableList()
        if (current.remove(packageName)) {
            saveFavorites(current)
        }
    }

    fun moveFavorite(fromIndex: Int, toIndex: Int) {
        val current = getFavoritePackageNames().toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            saveFavorites(current)
        }
    }

    fun getRecentPackageNames(): List<String> {
        val raw = prefs.getString(KEY_RECENT_PACKAGES, null)
        return if (raw.isNullOrBlank()) {
            emptyList()
        } else {
            raw.split(",").filter { it.isNotBlank() }
        }
    }

    fun recordRecentApp(packageName: String) {
        val current = getRecentPackageNames().toMutableList()
        current.remove(packageName)
        current.add(0, packageName)
        // Keep up to 20 most recent
        val trimmed = current.take(20)
        prefs.edit().putString(KEY_RECENT_PACKAGES, trimmed.joinToString(",")).apply()
    }

    fun removeRecentApp(packageName: String) {
        val current = getRecentPackageNames().toMutableList()
        if (current.remove(packageName)) {
            prefs.edit().putString(KEY_RECENT_PACKAGES, current.joinToString(",")).apply()
        }
    }

    fun clearAllRecentApps() {
        prefs.edit().remove(KEY_RECENT_PACKAGES).apply()
    }

    fun getSystemMemoryInfo(): MemoryStatusInfo {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)

            val availBytes = memInfo.availMem
            val totalBytes = memInfo.totalMem
            val usedBytes = (totalBytes - availBytes).coerceAtLeast(0L)

            val availGb = availBytes / (1024.0 * 1024.0 * 1024.0)
            val totalGb = totalBytes / (1024.0 * 1024.0 * 1024.0)
            val percent = if (totalBytes > 0) ((availBytes.toDouble() / totalBytes.toDouble()) * 100).toInt() else 50

            MemoryStatusInfo(
                usedBytes = usedBytes,
                availableBytes = availBytes,
                totalBytes = totalBytes,
                availableGbString = String.format(java.util.Locale.US, "%.1f GB", availGb),
                totalGbString = String.format(java.util.Locale.US, "%.1f GB", totalGb),
                percentFree = percent
            )
        } catch (e: Exception) {
            MemoryStatusInfo()
        }
    }

    suspend fun getRecentAppsList(installedApps: List<InstalledApp>): List<RecentAppItem> = withContext(Dispatchers.IO) {
        val installedMap = installedApps.associateBy { it.packageName }
        val recentPackages = getRecentPackageNames().toMutableList()

        // Fallback: If user hasn't launched apps inside the session yet, populate with prominent installed apps
        if (recentPackages.isEmpty()) {
            installedApps.take(6).forEach { recentPackages.add(it.packageName) }
        }

        val paletteColors = listOf(
            0xFF2E3E37, 0xFF354449, 0xFF4A3E30, 0xFF3C3048, 0xFF283A42, 0xFF482B32
        )

        recentPackages.mapIndexedNotNull { index, pkgName ->
            val app = installedMap[pkgName]
            if (app != null) {
                RecentAppItem(
                    packageName = app.packageName,
                    activityName = app.activityName,
                    label = app.label,
                    icon = app.icon,
                    lastUsedTimestamp = System.currentTimeMillis() - (index * 45000L),
                    memoryUsageMb = 80 + (index * 24) % 180,
                    accentColor = paletteColors[index % paletteColors.size]
                )
            } else null
        }
    }

    fun getPreferences(): LauncherPreferences {
        val posStr = prefs.getString(KEY_CLOCK_POS, ClockPosition.TOP_LEFT.name)
        val pos = try {
            ClockPosition.valueOf(posStr ?: ClockPosition.TOP_LEFT.name)
        } catch (e: Exception) {
            ClockPosition.TOP_LEFT
        }

        val fontStr = prefs.getString(KEY_CLOCK_FONT, com.example.model.ClockFontStyle.MINIMAL_LIGHT.name)
        val clockFont = try {
            com.example.model.ClockFontStyle.valueOf(fontStr ?: com.example.model.ClockFontStyle.MINIMAL_LIGHT.name)
        } catch (e: Exception) {
            com.example.model.ClockFontStyle.MINIMAL_LIGHT
        }

        val themedColorStr = prefs.getString(KEY_THEMED_ICON_COLOR, com.example.model.ThemedIconColor.DARK_CHARCOAL.name)
        val themedColor = try {
            com.example.model.ThemedIconColor.valueOf(themedColorStr ?: com.example.model.ThemedIconColor.DARK_CHARCOAL.name)
        } catch (e: Exception) {
            com.example.model.ThemedIconColor.DARK_CHARCOAL
        }

        val themedColorModeStr = prefs.getString(KEY_THEMED_ICON_COLOR_MODE, com.example.model.ThemedIconColorMode.AUTO_WALLPAPER.name)
        val themedColorMode = try {
            com.example.model.ThemedIconColorMode.valueOf(themedColorModeStr ?: com.example.model.ThemedIconColorMode.AUTO_WALLPAPER.name)
        } catch (e: Exception) {
            com.example.model.ThemedIconColorMode.AUTO_WALLPAPER
        }

        val customColorHex = prefs.getLong(KEY_CUSTOM_THEMED_COLOR_HEX, 0xFF4C6B61L)

        val themedStyleStr = prefs.getString(KEY_THEMED_ICON_STYLE, com.example.model.ThemedIconStyle.SMART_MINIMAL.name)
        val themedStyle = try {
            com.example.model.ThemedIconStyle.valueOf(themedStyleStr ?: com.example.model.ThemedIconStyle.SMART_MINIMAL.name)
        } catch (e: Exception) {
            com.example.model.ThemedIconStyle.SMART_MINIMAL
        }

        val gridStr = prefs.getString(KEY_HOME_GRID_LAYOUT, HomeGridLayout.GRID_4x6.name)
        val grid = try {
            HomeGridLayout.valueOf(gridStr ?: HomeGridLayout.GRID_4x6.name)
        } catch (e: Exception) {
            HomeGridLayout.GRID_4x6
        }

        val modeStr = prefs.getString(KEY_HOME_SCREEN_MODE, HomeScreenMode.VERTICAL_DOCK.name)
        val homeMode = try {
            HomeScreenMode.valueOf(modeStr ?: HomeScreenMode.VERTICAL_DOCK.name)
        } catch (e: Exception) {
            HomeScreenMode.VERTICAL_DOCK
        }

        val recentsOriStr = prefs.getString(KEY_RECENTS_ORIENTATION, RecentsOrientation.HORIZONTAL.name)
        val recentsOri = try {
            RecentsOrientation.valueOf(recentsOriStr ?: RecentsOrientation.HORIZONTAL.name)
        } catch (e: Exception) {
            RecentsOrientation.HORIZONTAL
        }

        val sortOrderStr = prefs.getString(KEY_APP_SORT_ORDER, AppSortOrder.ALPHABETICAL.name)
        val sortOrder = try {
            AppSortOrder.valueOf(sortOrderStr ?: AppSortOrder.ALPHABETICAL.name)
        } catch (e: Exception) {
            AppSortOrder.ALPHABETICAL
        }

        val lockClockFontStr = prefs.getString(KEY_LOCK_CLOCK_FONT, com.example.model.ClockFontStyle.MINIMAL_LIGHT.name)
        val lockClockFont = try {
            com.example.model.ClockFontStyle.valueOf(lockClockFontStr ?: com.example.model.ClockFontStyle.MINIMAL_LIGHT.name)
        } catch (e: Exception) {
            com.example.model.ClockFontStyle.MINIMAL_LIGHT
        }

        val lockClockPosStr = prefs.getString(KEY_LOCK_CLOCK_POS, ClockPosition.TOP_CENTER.name)
        val lockClockPos = try {
            ClockPosition.valueOf(lockClockPosStr ?: ClockPosition.TOP_CENTER.name)
        } catch (e: Exception) {
            ClockPosition.TOP_CENTER
        }

        val leftShortcutStr = prefs.getString(KEY_LOCK_LEFT_SHORTCUT, com.example.model.LockShortcut.FLASHLIGHT.name)
        val leftShortcut = try {
            com.example.model.LockShortcut.valueOf(leftShortcutStr ?: com.example.model.LockShortcut.FLASHLIGHT.name)
        } catch (e: Exception) {
            com.example.model.LockShortcut.FLASHLIGHT
        }

        val rightShortcutStr = prefs.getString(KEY_LOCK_RIGHT_SHORTCUT, com.example.model.LockShortcut.CAMERA.name)
        val rightShortcut = try {
            com.example.model.LockShortcut.valueOf(rightShortcutStr ?: com.example.model.LockShortcut.CAMERA.name)
        } catch (e: Exception) {
            com.example.model.LockShortcut.CAMERA
        }

        return LauncherPreferences(
            selectedWallpaperId = prefs.getString(KEY_WALLPAPER_ID, LauncherWallpaper.DEFAULT.id) ?: LauncherWallpaper.DEFAULT.id,
            homeScreenMode = homeMode,
            homeGridLayout = grid,
            fillCellsOfUninstalledApps = prefs.getBoolean(KEY_FILL_UNINSTALLED_CELLS, false),
            lockHomeScreenLayout = prefs.getBoolean(KEY_LOCK_HOME_LAYOUT, false),
            recentsOrientation = recentsOri,
            showMemoryStatus = prefs.getBoolean(KEY_SHOW_MEMORY_STATUS, true),
            blurAppPreviews = prefs.getBoolean(KEY_BLUR_APP_PREVIEWS, false),
            iconSizeDp = prefs.getInt(KEY_ICON_SIZE, 44),
            showAppNames = prefs.getBoolean(KEY_SHOW_NAMES, true),
            itemSpacingDp = prefs.getInt(KEY_ITEM_SPACING, 18),
            clockSizeSp = prefs.getInt(KEY_CLOCK_SIZE, 58),
            clockPosition = pos,
            clockFontStyle = clockFont,
            dimBackgroundPercent = prefs.getInt(KEY_DIM_BG, 15),
            useLeftDockScrim = prefs.getBoolean(KEY_LEFT_SCRIM, true),
            showWeather = prefs.getBoolean(KEY_SHOW_WEATHER, true),
            showAllAppsOnHome = prefs.getBoolean(KEY_SHOW_ALL_APPS, true),
            enableSound = prefs.getBoolean(KEY_ENABLE_SOUND, true),
            enableHaptic = prefs.getBoolean(KEY_ENABLE_HAPTIC, true),
            autoApplySystemWallpaper = prefs.getBoolean(KEY_AUTO_APPLY_WALLPAPER, true),
            themedIcons = prefs.getBoolean(KEY_THEMED_ICONS, true),
            themedIconColorMode = themedColorMode,
            themedIconColor = themedColor,
            customThemedIconColorHex = customColorHex,
            themedIconStyle = themedStyle,
            enableFisheyeScroll = prefs.getBoolean(KEY_FISHEYE_SCROLL, true),
            fisheyeMagnification = prefs.getFloat(KEY_FISHEYE_MAG, 1.35f),
            appSortOrder = sortOrder,
            // LOCK SCREEN PREFERENCES
            lockScreenEnabled = prefs.getBoolean(KEY_LOCK_ENABLED, true),
            lockClock24Hour = prefs.getBoolean(KEY_LOCK_CLOCK_24H, false),
            lockClockFontStyle = lockClockFont,
            lockClockSizeSp = prefs.getInt(KEY_LOCK_CLOCK_SIZE, 64),
            lockClockPosition = lockClockPos,
            lockShowDate = prefs.getBoolean(KEY_LOCK_SHOW_DATE, true),
            lockShowSeconds = prefs.getBoolean(KEY_LOCK_SHOW_SECONDS, false),
            lockShowWeather = prefs.getBoolean(KEY_LOCK_SHOW_WEATHER, true),
            lockShowNotifications = prefs.getBoolean(KEY_LOCK_SHOW_NOTIFS, true),
            lockShowNotificationContent = prefs.getBoolean(KEY_LOCK_SHOW_NOTIF_CONTENT, true),
            lockHideSensitiveNotifications = prefs.getBoolean(KEY_LOCK_HIDE_SENSITIVE, false),
            lockShowNotificationCount = prefs.getBoolean(KEY_LOCK_SHOW_NOTIF_COUNT, true),
            lockLeftShortcut = leftShortcut,
            lockRightShortcut = rightShortcut,
            lockDimPercent = prefs.getInt(KEY_LOCK_DIM, 15),
            lockEnableAnimations = prefs.getBoolean(KEY_LOCK_ENABLE_ANIM, true),
            lockAnimationSpeedMs = prefs.getInt(KEY_LOCK_ANIM_SPEED, 300)
        )
    }

    suspend fun applyWallpaperToSystem(
        wallpaper: LauncherWallpaper,
        applyToLockScreen: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            val bitmap = BitmapFactory.decodeResource(context.resources, wallpaper.drawableRes)
            if (bitmap != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    var flags = WallpaperManager.FLAG_SYSTEM
                    if (applyToLockScreen) {
                        flags = flags or WallpaperManager.FLAG_LOCK
                    }
                    wallpaperManager.setBitmap(bitmap, null, true, flags)
                } else {
                    wallpaperManager.setBitmap(bitmap)
                }
                true
            } else {
                wallpaperManager.setResource(wallpaper.drawableRes)
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting system wallpaper: ${wallpaper.title}", e)
            false
        }
    }

    fun savePreferences(p: LauncherPreferences) {
        prefs.edit()
            .putString(KEY_WALLPAPER_ID, p.selectedWallpaperId)
            .putString(KEY_HOME_SCREEN_MODE, p.homeScreenMode.name)
            .putString(KEY_HOME_GRID_LAYOUT, p.homeGridLayout.name)
            .putBoolean(KEY_FILL_UNINSTALLED_CELLS, p.fillCellsOfUninstalledApps)
            .putBoolean(KEY_LOCK_HOME_LAYOUT, p.lockHomeScreenLayout)
            .putString(KEY_RECENTS_ORIENTATION, p.recentsOrientation.name)
            .putBoolean(KEY_SHOW_MEMORY_STATUS, p.showMemoryStatus)
            .putBoolean(KEY_BLUR_APP_PREVIEWS, p.blurAppPreviews)
            .putInt(KEY_ICON_SIZE, p.iconSizeDp)
            .putBoolean(KEY_SHOW_NAMES, p.showAppNames)
            .putInt(KEY_ITEM_SPACING, p.itemSpacingDp)
            .putInt(KEY_CLOCK_SIZE, p.clockSizeSp)
            .putString(KEY_CLOCK_POS, p.clockPosition.name)
            .putString(KEY_CLOCK_FONT, p.clockFontStyle.name)
            .putInt(KEY_DIM_BG, p.dimBackgroundPercent)
            .putBoolean(KEY_LEFT_SCRIM, p.useLeftDockScrim)
            .putBoolean(KEY_SHOW_WEATHER, p.showWeather)
            .putBoolean(KEY_SHOW_ALL_APPS, p.showAllAppsOnHome)
            .putBoolean(KEY_ENABLE_SOUND, p.enableSound)
            .putBoolean(KEY_ENABLE_HAPTIC, p.enableHaptic)
            .putBoolean(KEY_AUTO_APPLY_WALLPAPER, p.autoApplySystemWallpaper)
            .putBoolean(KEY_THEMED_ICONS, p.themedIcons)
            .putString(KEY_THEMED_ICON_COLOR_MODE, p.themedIconColorMode.name)
            .putString(KEY_THEMED_ICON_COLOR, p.themedIconColor.name)
            .putLong(KEY_CUSTOM_THEMED_COLOR_HEX, p.customThemedIconColorHex)
            .putString(KEY_THEMED_ICON_STYLE, p.themedIconStyle.name)
            .putBoolean(KEY_FISHEYE_SCROLL, p.enableFisheyeScroll)
            .putFloat(KEY_FISHEYE_MAG, p.fisheyeMagnification)
            .putString(KEY_APP_SORT_ORDER, p.appSortOrder.name)
            // LOCK SCREEN PREFS
            .putBoolean(KEY_LOCK_ENABLED, p.lockScreenEnabled)
            .putBoolean(KEY_LOCK_CLOCK_24H, p.lockClock24Hour)
            .putString(KEY_LOCK_CLOCK_FONT, p.lockClockFontStyle.name)
            .putInt(KEY_LOCK_CLOCK_SIZE, p.lockClockSizeSp)
            .putString(KEY_LOCK_CLOCK_POS, p.lockClockPosition.name)
            .putBoolean(KEY_LOCK_SHOW_DATE, p.lockShowDate)
            .putBoolean(KEY_LOCK_SHOW_SECONDS, p.lockShowSeconds)
            .putBoolean(KEY_LOCK_SHOW_WEATHER, p.lockShowWeather)
            .putBoolean(KEY_LOCK_SHOW_NOTIFS, p.lockShowNotifications)
            .putBoolean(KEY_LOCK_SHOW_NOTIF_CONTENT, p.lockShowNotificationContent)
            .putBoolean(KEY_LOCK_HIDE_SENSITIVE, p.lockHideSensitiveNotifications)
            .putBoolean(KEY_LOCK_SHOW_NOTIF_COUNT, p.lockShowNotificationCount)
            .putString(KEY_LOCK_LEFT_SHORTCUT, p.lockLeftShortcut.name)
            .putString(KEY_LOCK_RIGHT_SHORTCUT, p.lockRightShortcut.name)
            .putInt(KEY_LOCK_DIM, p.lockDimPercent)
            .putBoolean(KEY_LOCK_ENABLE_ANIM, p.lockEnableAnimations)
            .putInt(KEY_LOCK_ANIM_SPEED, p.lockAnimationSpeedMs)
            .apply()
    }

    private var isTorchOn = false

    fun toggleFlashlight(): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? android.hardware.camera2.CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull()
            if (cameraManager != null && cameraId != null) {
                isTorchOn = !isTorchOn
                cameraManager.setTorchMode(cameraId, isTorchOn)
                isTorchOn
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling flashlight", e)
            false
        }
    }

    fun getBatteryStatus(): com.example.model.BatteryStatusInfo {
        return try {
            val intentFilter = android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)
            val level: Int = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status: Int = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == android.os.BatteryManager.BATTERY_STATUS_FULL

            val pct = if (level != -1 && scale != -1) {
                ((level.toFloat() / scale.toFloat()) * 100).toInt()
            } else {
                88
            }
            com.example.model.BatteryStatusInfo(levelPercent = pct, isCharging = isCharging)
        } catch (e: Exception) {
            com.example.model.BatteryStatusInfo(levelPercent = 88, isCharging = false)
        }
    }

    fun isNotificationListenerAccessGranted(): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(packageName)
    }

    fun openNotificationListenerSettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open Notification Listener Settings", e)
        }
    }
}
