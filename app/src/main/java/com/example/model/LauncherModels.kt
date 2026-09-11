package com.example.model

import android.graphics.drawable.Drawable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R

data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false,
    val installTime: Long = 0L
)

enum class ClockPosition {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT
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

enum class ThemedIconColor(val displayName: String, val colorHex: Long) {
    DARK_CHARCOAL("Dark Charcoal", 0xFF141918),
    LIGHT_SAGE("Muted Sage", 0xFFDFECE7),
    PURE_WHITE("Clean White", 0xFFFFFFFF),
    WARM_GOLD("Warm Amber", 0xFFF2D184)
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

data class LauncherPreferences(
    val selectedWallpaperId: String = LauncherWallpaper.DEFAULT.id,
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
    val enableClickSound: Boolean = true,  // Native click sound & haptic feedback on select
    val themedIcons: Boolean = true,       // Transform app icons to wallpaper/theme-matching silhouette
    val themedIconColor: ThemedIconColor = ThemedIconColor.DARK_CHARCOAL,
    val themedIconStyle: ThemedIconStyle = ThemedIconStyle.SMART_MINIMAL,
    val enableFisheyeScroll: Boolean = true, // Dynamic 1, 2, 3, 4, 3, 2, 1 magnifying lens zoom while scrolling
    val fisheyeMagnification: Float = 1.35f  // Center peak magnification scale (e.g. 1.25x - 1.55x)
) {
    val selectedWallpaper: LauncherWallpaper
        get() = LauncherWallpaper.fromId(selectedWallpaperId)
}
