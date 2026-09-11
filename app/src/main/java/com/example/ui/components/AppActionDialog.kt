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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import com.example.model.AppActionOrigin
import com.example.model.InstalledApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppActionDialog(
    isOpen: Boolean,
    app: InstalledApp?,
    origin: AppActionOrigin = AppActionOrigin.HOME_SCREEN,
    isOnHomeScreen: Boolean = true,
    isLayoutLocked: Boolean = false,
    onLaunch: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
    onAddToHome: () -> Unit,
    onRemoveFromHome: () -> Unit,
    onRename: () -> Unit,
    onAddToFolder: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen || app == null) return

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
                .padding(horizontal = 22.dp, vertical = 8.dp)
        ) {
            // App Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AppIconImage(
                    drawable = app.icon,
                    contentDescription = app.displayName,
                    size = 50.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.displayName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (app.customLabel != null && app.customLabel.isNotBlank()) {
                        Text(
                            text = "Original: ${app.label}",
                            color = Color(0xFF8AB4F8),
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = app.packageName,
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 11.sp
                    )
                }
            }

            if (isLayoutLocked) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x22FFA726))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Home layout is locked. Reordering and removal are disabled.",
                        color = Color(0xFFFFA726),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(6.dp))

            // 1. Open App (Always available)
            ActionMenuItem(
                icon = Icons.Default.Launch,
                iconTint = Color(0xFF81C784),
                title = "Open",
                subtitle = "Launch this application",
                testTag = "app_action_open",
                onClick = {
                    onDismiss()
                    onLaunch()
                }
            )

            // 2. App Info (Real Android Settings)
            ActionMenuItem(
                icon = Icons.Default.Info,
                iconTint = Color(0xFF8AB4F8),
                title = "App Info",
                subtitle = "Storage, permissions & system details",
                testTag = "app_action_info",
                onClick = {
                    onDismiss()
                    onAppInfo()
                }
            )

            // 3. Rename App
            ActionMenuItem(
                icon = Icons.Default.DriveFileRenameOutline,
                iconTint = Color(0xFFDFECE7),
                title = "Rename",
                subtitle = "Customize app display label",
                testTag = "app_action_rename",
                onClick = {
                    onDismiss()
                    onRename()
                }
            )

            // 4. Create Folder / Add to Folder
            ActionMenuItem(
                icon = Icons.Default.Folder,
                iconTint = Color(0xFFF2D184),
                title = "Add to Folder",
                subtitle = "Organize into folders",
                testTag = "app_action_folder",
                onClick = {
                    onDismiss()
                    onAddToFolder()
                }
            )

            // 5. Add / Remove from Home
            if (isOnHomeScreen || origin == AppActionOrigin.HOME_SCREEN) {
                ActionMenuItem(
                    icon = Icons.Default.RemoveCircleOutline,
                    iconTint = if (isLayoutLocked) Color.White.copy(alpha = 0.3f) else Color(0xFFFFB74D),
                    title = "Remove from Home",
                    subtitle = if (isLayoutLocked) "Locked" else "Remove shortcut from home",
                    enabled = !isLayoutLocked,
                    testTag = "app_action_remove_home",
                    onClick = {
                        onDismiss()
                        onRemoveFromHome()
                    }
                )
            } else {
                ActionMenuItem(
                    icon = Icons.Default.Add,
                    iconTint = if (isLayoutLocked) Color.White.copy(alpha = 0.3f) else Color(0xFF6F8E83),
                    title = "Add to Home",
                    subtitle = if (isLayoutLocked) "Locked" else "Pin to home favorites list",
                    enabled = !isLayoutLocked,
                    testTag = "app_action_add_home",
                    onClick = {
                        onDismiss()
                        onAddToHome()
                    }
                )
            }

            // 6. Move Up / Down (if on home)
            if (origin == AppActionOrigin.HOME_SCREEN && isOnHomeScreen && !isLayoutLocked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        onClick = onMoveUp,
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x18FFFFFF),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("app_action_move_up")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Move Up", color = Color.White, fontSize = 13.sp)
                        }
                    }

                    Surface(
                        onClick = onMoveDown,
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x18FFFFFF),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("app_action_move_down")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Move Down", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // 7. Uninstall (Real Android Delete Intent)
            ActionMenuItem(
                icon = Icons.Default.DeleteOutline,
                iconTint = Color(0xFFFF6E6E),
                title = "Uninstall",
                subtitle = "Prompt system uninstallation",
                testTag = "app_action_uninstall",
                onClick = {
                    onDismiss()
                    onUninstall()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun ActionMenuItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 9.dp, horizontal = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (enabled) iconTint.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) iconTint else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.4f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = if (enabled) Color.White.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.25f),
                fontSize = 11.sp
            )
        }
    }
}
