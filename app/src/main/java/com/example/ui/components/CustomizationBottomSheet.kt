package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LauncherRepository
import com.example.model.ClockPosition
import com.example.model.LauncherPreferences
import com.example.model.LauncherWallpaper
import com.example.util.LauncherManagerUtil
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()
    val repo = LauncherRepository(context)
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = if (isDefault) Color(0xFF7FA89B) else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Default Home Launcher",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            color = if (isDefault) Color(0xFF4C6B61) else Color(0x33FFFFFF),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isDefault) "Active" else "Set Default",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isDefault) {
                                "NXV Launcher is your active default home app. Tap to reconfigure."
                            } else {
                                "Tap here to select NXV Launcher as your default home app"
                            },
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )

                        IconButton(
                            onClick = { LauncherManagerUtil.openHomeAppSettings(context) },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0x22FFFFFF), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "System Home Settings",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
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

                Spacer(modifier = Modifier.height(14.dp))

                // Action to apply current wallpaper to Android system
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E2825),
                    border = BorderStroke(1.dp, Color(0x2E7FA89B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Wallpaper,
                                        contentDescription = null,
                                        tint = Color(0xFF7FA89B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sync System Wallpaper",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Keeps lock screen & background matching wallpaper when apps minimize",
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontSize = 12.sp
                                )
                            }

                            Switch(
                                checked = preferences.autoApplySystemWallpaper,
                                onCheckedChange = { isAuto ->
                                    val updated = preferences.copy(autoApplySystemWallpaper = isAuto)
                                    onPreferencesChange(updated)
                                    if (isAuto) {
                                        coroutineScope.launch {
                                            val ok = repo.applyWallpaperToSystem(preferences.selectedWallpaper)
                                            if (ok) {
                                                Toast.makeText(context, "System Wallpaper Applied!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4C6B61)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val ok = repo.applyWallpaperToSystem(preferences.selectedWallpaper)
                                    if (ok) {
                                        Toast.makeText(context, "Applied \"${preferences.selectedWallpaper.title}\" to System & Lock Screen", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Could not set system wallpaper", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4C6B61),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Apply to System & Lock Screen Now",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
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
                    Text("App Icon Palette Mode", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Mode Selection Chips (Auto Wallpaper / Preset Swatches / Universal Spectrum)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.example.model.ThemedIconColorMode.entries.forEach { modeOption ->
                            val isSelected = preferences.themedIconColorMode == modeOption
                            Surface(
                                color = if (isSelected) Color(0xFF4C6B61) else Color(0x22FFFFFF),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF7FA89B) else Color(0x33FFFFFF)),
                                modifier = Modifier.clickable {
                                    onPreferencesChange(preferences.copy(themedIconColorMode = modeOption))
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = when (modeOption) {
                                            com.example.model.ThemedIconColorMode.AUTO_WALLPAPER -> Icons.Default.AutoAwesome
                                            com.example.model.ThemedIconColorMode.PRESET_PALETTE -> Icons.Default.Palette
                                            com.example.model.ThemedIconColorMode.UNIVERSAL_CUSTOM -> Icons.Default.ColorLens
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = modeOption.displayName,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mode Specific UI
                    when (preferences.themedIconColorMode) {
                        com.example.model.ThemedIconColorMode.AUTO_WALLPAPER -> {
                            Surface(
                                color = Color(0x1E4C6B61),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color(0x337FA89B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(preferences.selectedWallpaper.suggestedThemedColor.colorHex))
                                            .border(1.5.dp, Color.White, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Auto Theme Matching Active",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Icons automatically adapt to \"${preferences.selectedWallpaper.title}\"",
                                            color = Color.White.copy(alpha = 0.65f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        com.example.model.ThemedIconColorMode.PRESET_PALETTE -> {
                            Text("Curated Color Swatches", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                com.example.model.ThemedIconColor.entries.forEach { colorOption ->
                                    val isSelected = preferences.themedIconColor == colorOption
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) Color(0xFF4C6B61) else Color(0x22FFFFFF))
                                            .clickable {
                                                onPreferencesChange(preferences.copy(
                                                    themedIconColor = colorOption,
                                                    themedIconColorMode = com.example.model.ThemedIconColorMode.PRESET_PALETTE
                                                ))
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
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
                        }

                        com.example.model.ThemedIconColorMode.UNIVERSAL_CUSTOM -> {
                            UniversalColorPicker(
                                currentHex = preferences.effectiveThemedIconColorHex,
                                onColorSelected = { newHex ->
                                    onPreferencesChange(
                                        preferences.copy(
                                            customThemedIconColorHex = newHex,
                                            themedIconColorMode = com.example.model.ThemedIconColorMode.UNIVERSAL_CUSTOM
                                        )
                                    )
                                }
                            )
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

            // HOME SCREEN LAYOUT SECTION (GRID & CELL MANAGEMENT)
            Surface(
                color = Color(0x22FFFFFF),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewStream,
                            contentDescription = null,
                            tint = Color(0xFF7FA89B),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Home Screen Style",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mode switch: Vertical List (Default) vs App Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Vertical Flow List (Signature Up/Down Scroll)
                        val isVertical = preferences.homeScreenMode == com.example.model.HomeScreenMode.VERTICAL_DOCK
                        Surface(
                            color = if (isVertical) Color(0xFF4C6B61) else Color(0x22FFFFFF),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isVertical) Color(0xFF7FA89B) else Color(0x33FFFFFF)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onPreferencesChange(
                                        preferences.copy(homeScreenMode = com.example.model.HomeScreenMode.VERTICAL_DOCK)
                                    )
                                }
                                .testTag("mode_vertical_list")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ViewStream,
                                        contentDescription = null,
                                        tint = if (isVertical) Color.White else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Vertical Flow",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = if (isVertical) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Up-down list scroll",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }

                        // 2. App Grid
                        val isGrid = preferences.homeScreenMode == com.example.model.HomeScreenMode.GRID
                        Surface(
                            color = if (isGrid) Color(0xFF4C6B61) else Color(0x22FFFFFF),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isGrid) Color(0xFF7FA89B) else Color(0x33FFFFFF)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onPreferencesChange(
                                        preferences.copy(homeScreenMode = com.example.model.HomeScreenMode.GRID)
                                    )
                                }
                                .testTag("mode_app_grid")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = null,
                                        tint = if (isGrid) Color.White else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "App Grid",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = if (isGrid) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${preferences.homeGridLayout.label} layout",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (preferences.homeScreenMode == com.example.model.HomeScreenMode.GRID) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Grid Layout Selector (4×5, 4×6, 5×5, 5×6, 5×7, 6×6)
                        Text(
                            text = "App Grid Size",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Choose columns and rows for home screen apps",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            com.example.model.HomeGridLayout.entries.forEach { gridOption ->
                                val isSelected = preferences.homeGridLayout == gridOption
                                Surface(
                                    color = if (isSelected) Color(0xFF4C6B61) else Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF7FA89B) else Color(0x33FFFFFF)
                                    ),
                                    modifier = Modifier
                                        .clickable {
                                            onPreferencesChange(
                                                preferences.copy(
                                                    homeGridLayout = gridOption,
                                                    homeScreenMode = com.example.model.HomeScreenMode.GRID
                                                )
                                            )
                                        }
                                        .testTag("grid_option_${gridOption.name}")
                                ) {
                                    Text(
                                        text = gridOption.label,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Fill cells of uninstalled apps toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "Fill cells of uninstalled apps",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Keep previous positions as empty cells to avoid automatic rearrangement",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }
                            Switch(
                                checked = preferences.fillCellsOfUninstalledApps,
                                onCheckedChange = {
                                    onPreferencesChange(preferences.copy(fillCellsOfUninstalledApps = it))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4C6B61)
                                ),
                                modifier = Modifier.testTag("toggle_fill_cells")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Lock Home screen layout toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Lock Home screen layout",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Prevent moving, reordering, or removing apps from home screen",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = preferences.lockHomeScreenLayout,
                            onCheckedChange = {
                                onPreferencesChange(preferences.copy(lockHomeScreenLayout = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4C6B61)
                            ),
                            modifier = Modifier.testTag("toggle_lock_home_layout")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // RECENTS / APP OVERVIEW SECTION
            Surface(
                color = Color(0x22FFFFFF),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewCarousel,
                            contentDescription = null,
                            tint = Color(0xFF7FA89B),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recents Overview",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Arrange items in Recents (Vertically / Horizontally)
                    Text(
                        text = "Arrange items in Recents",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Choose card deck scrolling direction",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        com.example.model.RecentsOrientation.entries.forEach { oriOption ->
                            val isSelected = preferences.recentsOrientation == oriOption
                            Surface(
                                color = if (isSelected) Color(0xFF4C6B61) else Color(0x22FFFFFF),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF7FA89B) else Color(0x33FFFFFF)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onPreferencesChange(preferences.copy(recentsOrientation = oriOption))
                                    }
                                    .testTag("recents_orientation_${oriOption.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (oriOption == com.example.model.RecentsOrientation.HORIZONTAL)
                                            Icons.Default.ViewCarousel else Icons.Default.ViewStream,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = oriOption.displayName,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Show memory status toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Show memory status",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Display real-time RAM usage & free capacity indicator",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = preferences.showMemoryStatus,
                            onCheckedChange = {
                                onPreferencesChange(preferences.copy(showMemoryStatus = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4C6B61)
                            ),
                            modifier = Modifier.testTag("toggle_show_memory")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Blur app previews toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Blur app previews",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Apply privacy blur to recent application window previews",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = preferences.blurAppPreviews,
                            onCheckedChange = {
                                onPreferencesChange(preferences.copy(blurAppPreviews = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4C6B61)
                            ),
                            modifier = Modifier.testTag("toggle_blur_previews")
                        )
                    }
                }
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

            // 2. Separate Audio Sound and Tactile Haptic Vibration Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Color(0xFF7FA89B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sound Effects", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    Text("Crisp audio click & scroll tick sounds on selection", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                }
                Switch(
                    checked = preferences.enableSound,
                    onCheckedChange = { onPreferencesChange(preferences.copy(enableSound = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4C6B61)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = Color(0xFF7FA89B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Haptic Vibration", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    Text("Tactile vibration feedback on tap, scroll, and gestures", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                }
                Switch(
                    checked = preferences.enableHaptic,
                    onCheckedChange = { onPreferencesChange(preferences.copy(enableHaptic = it)) },
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

            // ==================== NXV LOCK SCREEN CONFIGURATION ====================
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x331F2A27)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x447FA89B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF7FA89B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NXV Lock Screen",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Switch(
                            checked = preferences.lockScreenEnabled,
                            onCheckedChange = {
                                onPreferencesChange(preferences.copy(lockScreenEnabled = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4C6B61)
                            )
                        )
                    }

                    Text(
                        text = "Minimalist full-screen lock interface with customizable clock, shortcuts & live notifications",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )

                    if (preferences.lockScreenEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Clock Customization Sub-section
                        Text(
                            text = "CLOCK CUSTOMIZATION",
                            color = Color(0xFF7FA89B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 12/24 Hour Format Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("24-Hour Clock", color = Color.White, fontSize = 15.sp)
                            Switch(
                                checked = preferences.lockClock24Hour,
                                onCheckedChange = { onPreferencesChange(preferences.copy(lockClock24Hour = it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4C6B61)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Show Seconds
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Seconds", color = Color.White, fontSize = 15.sp)
                            Switch(
                                checked = preferences.lockShowSeconds,
                                onCheckedChange = { onPreferencesChange(preferences.copy(lockShowSeconds = it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4C6B61)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Show Date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Date", color = Color.White, fontSize = 15.sp)
                            Switch(
                                checked = preferences.lockShowDate,
                                onCheckedChange = { onPreferencesChange(preferences.copy(lockShowDate = it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4C6B61)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Show Lock Weather
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Weather", color = Color.White, fontSize = 15.sp)
                            Switch(
                                checked = preferences.lockShowWeather,
                                onCheckedChange = { onPreferencesChange(preferences.copy(lockShowWeather = it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4C6B61)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Clock Position Selector
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Clock Position", color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ClockPosition.values().forEach { posOption ->
                                    val isSelected = preferences.lockClockPosition == posOption
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) Color(0xFF4C6B61) else Color(0x22FFFFFF))
                                            .clickable { onPreferencesChange(preferences.copy(lockClockPosition = posOption)) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = posOption.displayName,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Lock Clock Size Slider
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Clock Size", color = Color.White, fontSize = 14.sp)
                                Text("${preferences.lockClockSizeSp} sp", color = Color(0xFF7FA89B), fontSize = 13.sp)
                            }
                            Slider(
                                value = preferences.lockClockSizeSp.toFloat(),
                                onValueChange = { onPreferencesChange(preferences.copy(lockClockSizeSp = it.toInt())) },
                                valueRange = 48f..88f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF7FA89B),
                                    activeTrackColor = Color(0xFF4C6B61),
                                    inactiveTrackColor = Color(0x33FFFFFF)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Notification Settings Sub-section
                        Text(
                            text = "NOTIFICATIONS",
                            color = Color(0xFF7FA89B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Lock Notifications", color = Color.White, fontSize = 15.sp)
                            Switch(
                                checked = preferences.lockShowNotifications,
                                onCheckedChange = { onPreferencesChange(preferences.copy(lockShowNotifications = it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4C6B61)
                                )
                            )
                        }

                        if (preferences.lockShowNotifications) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Hide Sensitive Content", color = Color.White, fontSize = 15.sp)
                                Switch(
                                    checked = preferences.lockHideSensitiveNotifications,
                                    onCheckedChange = { onPreferencesChange(preferences.copy(lockHideSensitiveNotifications = it)) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF4C6B61)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Show Notification Count", color = Color.White, fontSize = 15.sp)
                                Switch(
                                    checked = preferences.lockShowNotificationCount,
                                    onCheckedChange = { onPreferencesChange(preferences.copy(lockShowNotificationCount = it)) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF4C6B61)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Shortcuts Sub-section
                        Text(
                            text = "BOTTOM SHORTCUTS",
                            color = Color(0xFF7FA89B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Left Shortcut", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                com.example.model.LockShortcut.values().take(4).forEach { sc ->
                                    val isSel = preferences.lockLeftShortcut == sc
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) Color(0xFF4C6B61) else Color(0x15FFFFFF))
                                            .clickable { onPreferencesChange(preferences.copy(lockLeftShortcut = sc)) }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(sc.displayName, color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Right Shortcut", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                com.example.model.LockShortcut.values().take(4).forEach { sc ->
                                    val isSel = preferences.lockRightShortcut == sc
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) Color(0xFF4C6B61) else Color(0x15FFFFFF))
                                            .clickable { onPreferencesChange(preferences.copy(lockRightShortcut = sc)) }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(sc.displayName, color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Appearance & Dim Level
                        Text(
                            text = "APPEARANCE & DIM OVERLAY",
                            color = Color(0xFF7FA89B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Wallpaper Dark Dim Overlay", color = Color.White, fontSize = 14.sp)
                                Text("${preferences.lockDimPercent}%", color = Color(0xFF7FA89B), fontSize = 13.sp)
                            }
                            Slider(
                                value = preferences.lockDimPercent.toFloat(),
                                onValueChange = { onPreferencesChange(preferences.copy(lockDimPercent = it.toInt())) },
                                valueRange = 0f..75f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF7FA89B),
                                    activeTrackColor = Color(0xFF4C6B61),
                                    inactiveTrackColor = Color(0x33FFFFFF)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Animations & Speed
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Unlock Drag Animations", color = Color.White, fontSize = 15.sp)
                            Switch(
                                checked = preferences.lockEnableAnimations,
                                onCheckedChange = { onPreferencesChange(preferences.copy(lockEnableAnimations = it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4C6B61)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun UniversalColorPicker(
    currentHex: Long,
    onColorSelected: (Long) -> Unit
) {
    var hue by remember(currentHex) {
        val red = ((currentHex shr 16) and 0xFF) / 255f
        val green = ((currentHex shr 8) and 0xFF) / 255f
        val blue = (currentHex and 0xFF) / 255f
        val hsv = FloatArray(3)
        android.graphics.Color.RGBToHSV((red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt(), hsv)
        mutableStateOf(hsv[0])
    }

    var brightness by remember(currentHex) {
        val red = ((currentHex shr 16) and 0xFF) / 255f
        val green = ((currentHex shr 8) and 0xFF) / 255f
        val blue = (currentHex and 0xFF) / 255f
        val hsv = FloatArray(3)
        android.graphics.Color.RGBToHSV((red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt(), hsv)
        mutableStateOf(if (hsv[2] > 0.05f) hsv[2] else 0.85f)
    }

    var hexInput by remember(currentHex) {
        mutableStateOf(String.format("%06X", currentHex and 0xFFFFFFL))
    }

    val rainbowColors = remember {
        listOf(
            Color(0xFFFF0000),
            Color(0xFFFFFF00),
            Color(0xFF00FF00),
            Color(0xFF00FFFF),
            Color(0xFF0000FF),
            Color(0xFFFF00FF),
            Color(0xFFFF0000)
        )
    }

    Surface(
        color = Color(0x1AFFFFFF),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0x22FFFFFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(currentHex))
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Universal Color Spectrum",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "#${String.format("%06X", currentHex and 0xFFFFFFL)}",
                            color = Color(0xFF7FA89B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Hex Manual Input
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        val cleaned = input.take(6).filter { it.isLetterOrDigit() }.uppercase()
                        hexInput = cleaned
                        if (cleaned.length == 6) {
                            try {
                                val parsed = cleaned.toLong(16)
                                val finalHex = 0xFF000000L or parsed
                                onColorSelected(finalHex)
                            } catch (e: Exception) { }
                        }
                    },
                    modifier = Modifier
                        .width(100.dp)
                        .height(48.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    leadingIcon = {
                        Text("#", color = Color(0xFF7FA89B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0x22FFFFFF),
                        unfocusedContainerColor = Color(0x11FFFFFF),
                        focusedBorderColor = Color(0xFF7FA89B),
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Spectrum Drag Bar Label
            Text(
                text = "Drag finger across spectrum to select color",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Draggable Color Spectrum Bar
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.horizontalGradient(rainbowColors))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                            hue = fraction * 360f
                            val hsv = floatArrayOf(hue, 0.85f, brightness)
                            val argb = android.graphics.Color.HSVToColor(hsv)
                            val newHex = 0xFF000000L or (argb.toLong() and 0xFFFFFFL)
                            hexInput = String.format("%06X", newHex and 0xFFFFFFL)
                            onColorSelected(newHex)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                            hue = fraction * 360f
                            val hsv = floatArrayOf(hue, 0.85f, brightness)
                            val argb = android.graphics.Color.HSVToColor(hsv)
                            val newHex = 0xFF000000L or (argb.toLong() and 0xFFFFFFL)
                            hexInput = String.format("%06X", newHex and 0xFFFFFFL)
                            onColorSelected(newHex)
                        }
                    }
            ) {
                val thumbX = (hue / 360f) * maxWidth.value
                Box(
                    modifier = Modifier
                        .offset(x = (thumbX.dp - 12.dp).coerceAtLeast(0.dp))
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color(0xFF101413), CircleShape)
                        .align(Alignment.CenterStart)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Brightness / Shade Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shade",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.width(44.dp)
                )
                Slider(
                    value = brightness,
                    onValueChange = { b ->
                        brightness = b
                        val hsv = floatArrayOf(hue, 0.85f, brightness)
                        val argb = android.graphics.Color.HSVToColor(hsv)
                        val newHex = 0xFF000000L or (argb.toLong() and 0xFFFFFFL)
                        hexInput = String.format("%06X", newHex and 0xFFFFFFL)
                        onColorSelected(newHex)
                    },
                    valueRange = 0.15f..1f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color(0xFF7FA89B),
                        inactiveTrackColor = Color(0x33FFFFFF)
                    )
                )
            }
        }
    }
}
