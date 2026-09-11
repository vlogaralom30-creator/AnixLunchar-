package com.example.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import com.example.model.ClockPosition
import com.example.model.InstalledApp
import com.example.model.LauncherPreferences
import com.example.model.LauncherWallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections

class LauncherRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nxv_launcher_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FAVORITES = "favorite_package_names"
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
        private const val KEY_THEMED_ICONS = "pref_themed_icons"
        private const val KEY_THEMED_ICON_COLOR = "pref_themed_icon_color"
        private const val KEY_THEMED_ICON_STYLE = "pref_themed_icon_style"
        private const val KEY_FISHEYE_SCROLL = "pref_fisheye_scroll"
        private const val KEY_FISHEYE_MAG = "pref_fisheye_mag"
        private const val KEY_WALLPAPER_ID = "pref_wallpaper_id"
        private const val TAG = "NXVLauncherRepo"
    }

    // Cache icons in memory to avoid repeated heavy binder calls to PackageManager
    private val iconCache = Collections.synchronizedMap(mutableMapOf<String, Drawable>())

    suspend fun getInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
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
                    icon = icon,
                    isSystemApp = isSystem
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }

        apps
    }

    fun launchApp(app: InstalledApp): Boolean {
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

        val themedStyleStr = prefs.getString(KEY_THEMED_ICON_STYLE, com.example.model.ThemedIconStyle.SMART_MINIMAL.name)
        val themedStyle = try {
            com.example.model.ThemedIconStyle.valueOf(themedStyleStr ?: com.example.model.ThemedIconStyle.SMART_MINIMAL.name)
        } catch (e: Exception) {
            com.example.model.ThemedIconStyle.SMART_MINIMAL
        }

        return LauncherPreferences(
            selectedWallpaperId = prefs.getString(KEY_WALLPAPER_ID, LauncherWallpaper.DEFAULT.id) ?: LauncherWallpaper.DEFAULT.id,
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
            enableClickSound = prefs.getBoolean(KEY_ENABLE_SOUND, true),
            themedIcons = prefs.getBoolean(KEY_THEMED_ICONS, true),
            themedIconColor = themedColor,
            themedIconStyle = themedStyle,
            enableFisheyeScroll = prefs.getBoolean(KEY_FISHEYE_SCROLL, true),
            fisheyeMagnification = prefs.getFloat(KEY_FISHEYE_MAG, 1.35f)
        )
    }

    fun savePreferences(p: LauncherPreferences) {
        prefs.edit()
            .putString(KEY_WALLPAPER_ID, p.selectedWallpaperId)
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
            .putBoolean(KEY_ENABLE_SOUND, p.enableClickSound)
            .putBoolean(KEY_THEMED_ICONS, p.themedIcons)
            .putString(KEY_THEMED_ICON_COLOR, p.themedIconColor.name)
            .putString(KEY_THEMED_ICON_STYLE, p.themedIconStyle.name)
            .putBoolean(KEY_FISHEYE_SCROLL, p.enableFisheyeScroll)
            .putFloat(KEY_FISHEYE_MAG, p.fisheyeMagnification)
            .apply()
    }
}
