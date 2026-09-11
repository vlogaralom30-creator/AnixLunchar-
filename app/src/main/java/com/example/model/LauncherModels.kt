package com.example.model

import android.graphics.drawable.Drawable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

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

data class LauncherPreferences(
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
    val themedIconStyle: ThemedIconStyle = ThemedIconStyle.SMART_MINIMAL
)
