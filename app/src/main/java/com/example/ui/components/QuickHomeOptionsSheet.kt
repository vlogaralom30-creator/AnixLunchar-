package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HomeScreenMode
import com.example.model.LauncherPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickHomeOptionsSheet(
    isOpen: Boolean,
    preferences: LauncherPreferences,
    onOpenWallpaper: () -> Unit,
    onOpenHomeSettings: () -> Unit,
    onAddApp: () -> Unit,
    onToggleLayout: () -> Unit,
    onToggleLockLayout: () -> Unit,
    onLockScreen: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141917),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Home Options",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Quick launcher controls & settings",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lock Screen Option
            QuickOptionRow(
                icon = Icons.Default.Lock,
                iconTint = Color(0xFF7FA89B),
                title = "Lock Screen",
                subtitle = "Activate minimalist NXV lock screen",
                testTag = "quick_home_lock_screen",
                onClick = {
                    onDismiss()
                    onLockScreen()
                }
            )

            // Wallpaper Option
            QuickOptionRow(
                icon = Icons.Default.Image,
                iconTint = Color(0xFF8AB4F8),
                title = "Wallpaper",
                subtitle = "Choose background and visual style",
                testTag = "quick_home_wallpaper",
                onClick = {
                    onDismiss()
                    onOpenWallpaper()
                }
            )

            // Home Settings Option
            QuickOptionRow(
                icon = Icons.Default.Settings,
                iconTint = Color(0xFFDFECE7),
                title = "Home Settings",
                subtitle = "Clock, icons, sounds and layout fine-tuning",
                testTag = "quick_home_settings",
                onClick = {
                    onDismiss()
                    onOpenHomeSettings()
                }
            )

            // Add App Option
            QuickOptionRow(
                icon = Icons.Default.Add,
                iconTint = Color(0xFF6F8E83),
                title = "Add App",
                subtitle = "Select apps from drawer to pin on Home",
                testTag = "quick_home_add_app",
                onClick = {
                    onDismiss()
                    onAddApp()
                }
            )

            // Layout Mode Toggle
            val isGrid = preferences.homeScreenMode == HomeScreenMode.GRID
            QuickOptionRow(
                icon = if (isGrid) Icons.Default.ViewAgenda else Icons.Default.GridView,
                iconTint = Color(0xFFF2D184),
                title = "Switch Layout",
                subtitle = if (isGrid) "Current: Grid (Tap for Vertical List)" else "Current: Vertical List (Tap for Grid)",
                testTag = "quick_home_toggle_layout",
                onClick = {
                    onToggleLayout()
                }
            )

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Lock / Unlock Home Layout
            val isLocked = preferences.lockHomeScreenLayout
            QuickOptionRow(
                icon = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                iconTint = if (isLocked) Color(0xFFFF8B8B) else Color(0xFF81C784),
                title = if (isLocked) "Unlock Home Layout" else "Lock Home Layout",
                subtitle = if (isLocked) "Layout is locked (prevent drag/reordering)" else "Layout is unlocked (free to reorder)",
                testTag = "quick_home_toggle_lock",
                onClick = {
                    onToggleLockLayout()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickOptionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}
