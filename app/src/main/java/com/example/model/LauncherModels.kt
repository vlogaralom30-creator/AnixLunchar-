package com.example.model

import android.graphics.drawable.Drawable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R
import java.util.UUID

data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val customLabel: String? = null,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false,
    val installTime: Long = 0L,
    val launchCount: Int = 0,
    val lastLaunchTime: Long = 0L
) {
    val displayName: String
        get() = customLabel?.takeIf { it.isNotBlank() } ?: label
}

enum class AppSortOrder(val displayName: String, val subtitle: String) {
    ALPHABETICAL("Alphabetical", "A to Z"),
    RECENTLY_USED("Recently Used", "By last opened"),
    MOST_USED("Most Used", "By launch count")
}

data class AppFolder(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Folder",
    val packageNames: List<String> = emptyList()
)

sealed interface HomeItem {
    val id: String
    val displayName: String

    data class App(val app: InstalledApp) : HomeItem {
        override val id: String get() = "app:${app.packageName}"
        override val displayName: String get() = app.displayName
    }

    data class Folder(val folder: AppFolder, val apps: List<InstalledApp>) : HomeItem {
        override val id: String get() = "folder:${folder.id}"
        override val displayName: String get() = folder.name
    }
}

enum class AppActionOrigin {
    HOME_SCREEN,
    APP_DRAWER,
    FOLDER
}

enum class ClockPosition(val displayName: String) {
    TOP_LEFT("Top Left"),
    TOP_CENTER("Top Center"),
    TOP_RIGHT("Top Right")
}

enum class ClockFontStyle(
    val displayName: String,
    val fontFamily: FontFamily,
    val fontWeight: FontWeight,
    val letterSpacingSp: Float,
    val sampleText: String
) {
    MINIMAL_LIGHT("Minimal Light (Default)", FontFamily.SansSerif, FontWeight.Light, 1.2f, "12:45"),
    CLEAN_SANS("Clean Modern", FontFamily.SansSerif, FontWeight.Normal, 0.5f, "12:45"),
    BOLD_DISPLAY("Bold Punch", FontFamily.SansSerif, FontWeight.Bold, 0f, "12:45"),
    ELEGANT_SERIF("Elegant Serif", FontFamily.Serif, FontWeight.Normal, 1.0f, "12:45"),
    MONOSPACE_TECH("Monospace Tech", FontFamily.Monospace, FontWeight.Medium, 2.0f, "12:45"),
    CURSIVE_AESTHETIC("Cursive Soft", FontFamily.Cursive, FontWeight.Normal, 1.5f, "12:45")
}

enum class ThemedIconColorMode(val displayName: String, val subtitle: String) {
    AUTO_WALLPAPER("Auto Wallpaper", "Auto-extract palette from active wallpaper"),
    PRESET_PALETTE("Preset Palette", "Select from curated theme colors"),
    UNIVERSAL_CUSTOM("Universal Palette", "Drag spectrum slider or enter custom Hex")
}

enum class ThemedIconColor(val displayName: String, val colorHex: Long) {
    DARK_CHARCOAL("Dark Charcoal", 0xFF141918),
    LIGHT_SAGE("Muted Sage", 0xFFDFECE7),
    PURE_WHITE("Clean White", 0xFFFFFFFF),
    WARM_GOLD("Warm Amber", 0xFFF2D184),
    CYBER_CYAN("Cyber Cyan", 0xFF4DEEEA),
    SUNSET_PINK("Sunset Pink", 0xFFE056FD),
    EMERALD_MINT("Emerald Mint", 0xFF2ED573),
    DEEP_VIOLET("Deep Violet", 0xFF6C5CE7),
    ELECTRIC_YELLOW("Electric Yellow", 0xFFFFE119),
    CORAL_ORANGE("Coral Orange", 0xFFFF7675)
}

enum class ThemedIconStyle(val title: String, val description: String) {
    SMART_MINIMAL("Smart Minimal (Vector & Cutout)", "Crisp vector glyphs with smart foreground extraction"),
    TINTED_ADAPTIVE("Duotone Accent", "Theme badge base with sharp high-contrast symbol"),
    MUTED_MONOCHROME("Muted Full Color", "Desaturated true icon retaining full visual identity")
}

enum class LauncherWallpaper(
    val id: String,
    val title: String,
    val subtitle: String,
    val drawableRes: Int,
    val suggestedThemedColor: ThemedIconColor = ThemedIconColor.DARK_CHARCOAL
) {
    DEFAULT("default", "Nature Forest", "Default serene organic landscape", R.drawable.img_default_wallpaper_1789117027459, ThemedIconColor.DARK_CHARCOAL),
    THEME_1_CAT("theme_1_cat", "Cyber Cat", "Neon aesthetic cyber feline", R.drawable.theme_1_cat, ThemedIconColor.WARM_GOLD),
    THEME_2_BLUE_ANIME("theme_2_blue_anime", "Blue Anime", "Vibrant celestial blue anime", R.drawable.theme_2_blue_anime, ThemedIconColor.LIGHT_SAGE),
    THEME_3_BLACK_ANIME("theme_3_black_anime", "Black Anime", "Moody monochrome dark anime", R.drawable.theme_3_black_anime, ThemedIconColor.PURE_WHITE),
    THEME_4_KAKASHI("theme_4_kakashi", "Kakashi Sensei", "Iconic dark ninja aesthetic", R.drawable.theme_4_kakashi, ThemedIconColor.DARK_CHARCOAL),
    THEME_5_BLACK_YELLOW("theme_5_black_yellow", "Black & Yellow", "High-contrast golden cyber glow", R.drawable.theme_5_black_yellow, ThemedIconColor.WARM_GOLD),
    THEME_6_PINK_RED("theme_6_pink_red", "Pink & Red", "Neon crimson artistic atmosphere", R.drawable.theme_6_pink_red, ThemedIconColor.PURE_WHITE);

    companion object {
        fun fromId(id: String?): LauncherWallpaper {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
        }
    }
}

enum class HomeGridLayout(val label: String, val columns: Int, val rows: Int) {
    GRID_4x5("4×5", 4, 5),
    GRID_4x6("4×6", 4, 6),
    GRID_5x5("5×5", 5, 5),
    GRID_5x6("5×6", 5, 6),
    GRID_5x7("5×7", 5, 7),
    GRID_6x6("6×6", 6, 6);

    companion object {
        fun fromLabel(label: String?): HomeGridLayout {
            return entries.firstOrNull { it.label == label || it.name.equals(label, ignoreCase = true) } ?: GRID_4x6
        }
    }
}

enum class HomeScreenMode(val displayName: String) {
    GRID("App Grid"),
    VERTICAL_DOCK("Vertical List")
}

enum class RecentsOrientation(val displayName: String) {
    HORIZONTAL("Horizontal"),
    VERTICAL("Vertical")
}

data class RecentAppItem(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable? = null,
    val lastUsedTimestamp: Long = System.currentTimeMillis(),
    val memoryUsageMb: Int = 128,
    val accentColor: Long = 0xFF2A3A34
)

data class MemoryStatusInfo(
    val usedBytes: Long = 0L,
    val availableBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val availableGbString: String = "4.2 GB",
    val totalGbString: String = "8.0 GB",
    val percentFree: Int = 52
)

enum class LockShortcut(val displayName: String) {
    FLASHLIGHT("Flashlight"),
    CAMERA("Camera"),
    PHONE("Phone"),
    SEARCH("Search"),
    NONE("None")
}

data class LockNotificationItem(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val timeFormatted: String = "Now",
    val appIconDrawable: Drawable? = null,
    val isSensitive: Boolean = false
)

data class BatteryStatusInfo(
    val levelPercent: Int = 85,
    val isCharging: Boolean = false
)

data class LauncherPreferences(
    val selectedWallpaperId: String = LauncherWallpaper.DEFAULT.id,
    val homeScreenMode: HomeScreenMode = HomeScreenMode.VERTICAL_DOCK,
    val homeGridLayout: HomeGridLayout = HomeGridLayout.GRID_4x6,
    val fillCellsOfUninstalledApps: Boolean = false,
    val lockHomeScreenLayout: Boolean = false,
    val recentsOrientation: RecentsOrientation = RecentsOrientation.HORIZONTAL,
    val showMemoryStatus: Boolean = true,
    val blurAppPreviews: Boolean = false,
    val iconSizeDp: Int = 46,
    val showAppNames: Boolean = true,
    val itemSpacingDp: Int = 18,
    val clockSizeSp: Int = 54,
    val clockPosition: ClockPosition = ClockPosition.TOP_LEFT,
    val clockFontStyle: ClockFontStyle = ClockFontStyle.MINIMAL_LIGHT,
    val dimBackgroundPercent: Int = 20,
    val useLeftDockScrim: Boolean = true,
    val showWeather: Boolean = true,
    val showAllAppsOnHome: Boolean = true, // Shows all installed apps on home scroll list
    val enableSound: Boolean = true,       // Audio click and scroll tick sounds
    val enableHaptic: Boolean = true,      // Tactile vibration on tap and scroll
    val autoApplySystemWallpaper: Boolean = true, // Sets Android system wallpaper automatically on wallpaper select
    val themedIcons: Boolean = true,       // Transform app icons to wallpaper/theme-matching silhouette
    val themedIconColorMode: ThemedIconColorMode = ThemedIconColorMode.AUTO_WALLPAPER,
    val themedIconColor: ThemedIconColor = ThemedIconColor.DARK_CHARCOAL,
    val customThemedIconColorHex: Long = 0xFF4C6B61,
    val themedIconStyle: ThemedIconStyle = ThemedIconStyle.SMART_MINIMAL,
    val enableFisheyeScroll: Boolean = true, // Dynamic 1, 2, 3, 4, 3, 2, 1 magnifying lens zoom while scrolling
    val fisheyeMagnification: Float = 1.35f,  // Center peak magnification scale (e.g. 1.25x - 1.55x)
    val appSortOrder: AppSortOrder = AppSortOrder.ALPHABETICAL,
    // LOCK SCREEN PREFERENCES
    val lockScreenEnabled: Boolean = true,
    val lockClock24Hour: Boolean = false,
    val lockClockFontStyle: ClockFontStyle = ClockFontStyle.MINIMAL_LIGHT,
    val lockClockSizeSp: Int = 64,
    val lockClockPosition: ClockPosition = ClockPosition.TOP_CENTER,
    val lockShowDate: Boolean = true,
    val lockShowSeconds: Boolean = false,
    val lockShowWeather: Boolean = true,
    val lockShowNotifications: Boolean = true,
    val lockShowNotificationContent: Boolean = true,
    val lockHideSensitiveNotifications: Boolean = false,
    val lockShowNotificationCount: Boolean = true,
    val lockLeftShortcut: LockShortcut = LockShortcut.FLASHLIGHT,
    val lockRightShortcut: LockShortcut = LockShortcut.CAMERA,
    val lockDimPercent: Int = 15,
    val lockEnableAnimations: Boolean = true,
    val lockAnimationSpeedMs: Int = 300
) {
    val enableClickSound: Boolean
        get() = enableSound || enableHaptic

    val selectedWallpaper: LauncherWallpaper
        get() = LauncherWallpaper.fromId(selectedWallpaperId)

    val effectiveThemedIconColorHex: Long
        get() = when (themedIconColorMode) {
            ThemedIconColorMode.AUTO_WALLPAPER -> selectedWallpaper.suggestedThemedColor.colorHex
            ThemedIconColorMode.PRESET_PALETTE -> themedIconColor.colorHex
            ThemedIconColorMode.UNIVERSAL_CUSTOM -> if (customThemedIconColorHex != 0L) customThemedIconColorHex else themedIconColor.colorHex
        }
}
