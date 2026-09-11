package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClockPosition
import com.example.model.LauncherPreferences
import com.example.model.LauncherWallpaper
import com.example.util.LauncherManagerUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationBottomSheet(
    isOpen: Boolean,
    preferences: LauncherPreferences,
    onPreferencesChange: (LauncherPreferences) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF161C1B),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NXV Customization",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Default Home Launcher Selection Card & System Settings
            val isDefault = LauncherManagerUtil.isDefaultLauncher(context)
            Surface(
                color = if (isDefault) Color(0x284C6B61) else Color(0x22FFFFFF),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    if (isDefault) Color(0xFF7FA89B) else Color(0x44FFFFFF)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        LauncherManagerUtil.openDefaultLauncherChooser(context)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Default Home Launcher",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = if (isDefault) Color(0xFF4C6B61) else Color(0x33FFFFFF),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isDefault) "Active" else "Set Default",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isDefault) {
                                "NXV Launcher is your active default home app. Tap to reconfigure."
                            } else {
                                "Tap here to select NXV Launcher as your default home app"
                            },
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { LauncherManagerUtil.openHomeAppSettings(context) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0x22FFFFFF), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "System Home Settings",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Select Default Launcher",
                            tint = if (isDefault) Color(0xFF7FA89B) else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Wallpapers & Themes Gallery
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color(0xFF7FA89B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Themes & Wallpapers",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Choose your aesthetic wallpaper & theme preset",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(LauncherWallpaper.entries) { wallpaper ->
                        val isSelected = preferences.selectedWallpaperId == wallpaper.id
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(90.dp)
                                .clickable {
                                    onPreferencesChange(
                                        preferences.copy(
                                            selectedWallpaperId = wallpaper.id,
                                            themedIconColor = wallpaper.suggestedThemedColor
                                        )
                                    )
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 90.dp, height = 130.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF7FA89B) else Color(0x33FFFFFF),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                            ) {
                                Image(
                                    painter = painterResource(id = wallpaper.drawableRes),
                                    contentDescription = wallpaper.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Gradient overlay at bottom of card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(45.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                            )
                                        )
                                )

                                if (isSelected) {
                                    Surface(
                                        color = Color(0xFF4C6B61),
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.padding(3.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = wallpaper.title,
                                color = if (isSelected) Color(0xFF7FA89B) else Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 0. Themed Icons (Wallpaper Matching Silhouette)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text("Themed Icons", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("Transform app icons into wallpaper-matching dark silhouettes", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                }
                Switch(
                    checked = preferences.themedIcons,
                    onCheckedChange = { onPreferencesChange(preferences.copy(themedIcons = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4C6B61)
                    )
                )
            }

            if (preferences.themedIcons) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(start = 4.dp)) {
                    Text("Icon Silhouette Shade", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.example.model.ThemedIconColor.values().forEach { colorOption ->
                            val isSelected = preferences.themedIconColor == colorOption
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Color(0xFF4C6B61) else Color(0x22FFFFFF))
                                    .clickable { onPreferencesChange(preferences.copy(themedIconColor = colorOption)) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(colorOption.colorHex))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = colorOption.displayName,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Themed Icon Recognition Mode", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.example.model.ThemedIconStyle.values().forEach { styleOption ->
                            val isSelected = preferences.themedIconStyle == styleOption
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Color(0xFF4C6B61) else Color(0x22FFFFFF))
                                    .clickable { onPreferencesChange(preferences.copy(themedIconStyle = styleOption)) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = styleOption.title,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Show All Apps on Home
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text("Show All Apps on Home", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("Scroll up/down through all installed apps on home screen", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                }
                Switch(
                    checked = preferences.showAllAppsOnHome,
                    onCheckedChange = { onPreferencesChange(preferences.copy(showAllAppsOnHome = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4C6B61)
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1.5 Fisheye Lens Scroll Animation (1, 2, 3, 4, 3, 2, 1 Magnifying Wheel Effect)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text("Fisheye Lens Scroll Animation", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("Uniform 1, 1, 1, 1 when resting, dynamic 1, 2, 3, 4, 3, 2, 1 zoom while scrolling", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                }
                Switch(
                    checked = preferences.enableFisheyeScroll,
                    onCheckedChange = { onPreferencesChange(preferences.copy(enableFisheyeScroll = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4C6B61)
                    )
                )
            }

            if (preferences.enableFisheyeScroll) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(start = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Center Zoom Level", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        Text(
                            String.format(java.util.Locale.US, "%.2fx", preferences.fisheyeMagnification),
                            color = Color(0xFF7FA89B),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Slider(
                        value = preferences.fisheyeMagnification,
                        onValueChange = { onPreferencesChange(preferences.copy(fisheyeMagnification = it)) },
                        valueRange = 1.15f..1.60f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF7FA89B),
                            activeTrackColor = Color(0xFF4C6B61),
                            inactiveTrackColor = Color(0x33FFFFFF)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Click Sound & Haptic Feedback
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text("Click Sound & Haptic", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("Crisp click-click sound and tactile feel when selecting apps", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                }
                Switch(
                    checked = preferences.enableClickSound,
                    onCheckedChange = { onPreferencesChange(preferences.copy(enableClickSound = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4C6B61)
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. App Names Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Show App Names", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("Show text labels next to favorite icons", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                }
                Switch(
                    checked = preferences.showAppNames,
                    onCheckedChange = { onPreferencesChange(preferences.copy(showAppNames = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4C6B61)
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Weather Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Show Weather", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("Display temperature next to date", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                }
                Switch(
                    checked = preferences.showWeather,
                    onCheckedChange = { onPreferencesChange(preferences.copy(showWeather = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4C6B61)
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Left Dock Frosted Scrim Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Left Dock Vignette", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("Soft dark gradient behind vertical dock", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                }
                Switch(
                    checked = preferences.useLeftDockScrim,
                    onCheckedChange = { onPreferencesChange(preferences.copy(useLeftDockScrim = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4C6B61)
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Icon Size Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Icon Size", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("${preferences.iconSizeDp} dp", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                }
                Slider(
                    value = preferences.iconSizeDp.toFloat(),
                    onValueChange = { onPreferencesChange(preferences.copy(iconSizeDp = it.toInt())) },
                    valueRange = 36f..64f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF7FA89B),
                        activeTrackColor = Color(0xFF4C6B61),
                        inactiveTrackColor = Color(0x33FFFFFF)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. App Spacing Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Vertical App Spacing", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("${preferences.itemSpacingDp} dp", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                }
                Slider(
                    value = preferences.itemSpacingDp.toFloat(),
                    onValueChange = { onPreferencesChange(preferences.copy(itemSpacingDp = it.toInt())) },
                    valueRange = 10f..32f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF7FA89B),
                        activeTrackColor = Color(0xFF4C6B61),
                        inactiveTrackColor = Color(0x33FFFFFF)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Clock Size Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Clock Size", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("${preferences.clockSizeSp} sp", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                }
                Slider(
                    value = preferences.clockSizeSp.toFloat(),
                    onValueChange = { onPreferencesChange(preferences.copy(clockSizeSp = it.toInt())) },
                    valueRange = 40f..80f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF7FA89B),
                        activeTrackColor = Color(0xFF4C6B61),
                        inactiveTrackColor = Color(0x33FFFFFF)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Clock Font Style Selector
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Minimalist Clock Font Style", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text("Choose typography aesthetic to match your wallpaper & theme", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    com.example.model.ClockFontStyle.values().forEach { fontOption ->
                        val isSelected = preferences.clockFontStyle == fontOption
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFF4C6B61) else Color(0x22FFFFFF))
                                .clickable { onPreferencesChange(preferences.copy(clockFontStyle = fontOption)) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = fontOption.sampleText,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontFamily = fontOption.fontFamily,
                                    fontWeight = fontOption.fontWeight,
                                    letterSpacing = fontOption.letterSpacingSp.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = fontOption.displayName,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
