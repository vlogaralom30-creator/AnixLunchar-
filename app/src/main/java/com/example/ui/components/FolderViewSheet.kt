package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppFolder
import com.example.model.InstalledApp
import com.example.model.LauncherPreferences

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FolderViewSheet(
    isOpen: Boolean,
    folder: AppFolder?,
    apps: List<InstalledApp>,
    preferences: LauncherPreferences,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit,
    onRemoveAppFromFolder: (AppFolder, InstalledApp) -> Unit,
    onRenameFolder: (AppFolder, String) -> Unit,
    onDeleteFolder: (AppFolder) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen || folder == null) return

    val sheetState = rememberModalBottomSheetState()
    var isRenamingFolder by remember { mutableStateOf(false) }

    val appMap = remember(apps) { apps.associateBy { it.packageName } }
    val appsInFolder = remember(folder, appMap) {
        folder.packageNames.mapNotNull { appMap[it] }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141A17),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Folder Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6F8E83).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color(0xFF6F8E83),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${appsInFolder.size} apps inside",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }

                // Rename Button
                IconButton(
                    onClick = { isRenamingFolder = true },
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .testTag("folder_rename_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Rename Folder",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Delete Button
                IconButton(
                    onClick = { onDeleteFolder(folder) },
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0x22FF5252), CircleShape)
                        .testTag("folder_delete_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Folder",
                        tint = Color(0xFFFF6E6E),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Grid in Folder
            if (appsInFolder.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "This folder is empty.\nLong press any app to add it.",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(appsInFolder, key = { it.packageName }) { app ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    onClick = {
                                        onAppClick(app)
                                        onDismiss()
                                    },
                                    onLongClick = {
                                        onAppLongClick(app)
                                    }
                                )
                                .padding(vertical = 6.dp, horizontal = 2.dp)
                                .testTag("folder_app_item_${app.packageName}")
                        ) {
                            AppIconImage(
                                drawable = app.icon,
                                contentDescription = app.displayName,
                                packageName = app.packageName,
                                themed = preferences.themedIcons,
                                themeColor = Color(preferences.effectiveThemedIconColorHex),
                                iconStyle = preferences.themedIconStyle,
                                size = preferences.iconSizeDp.dp
                            )

                            if (preferences.showAppNames) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = app.displayName,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (isRenamingFolder) {
        CreateFolderDialog(
            isOpen = true,
            initialName = folder.name,
            title = "Rename Folder",
            confirmButtonText = "Save",
            onConfirm = { newName ->
                onRenameFolder(folder, newName)
                isRenamingFolder = false
            },
            onDismiss = { isRenamingFolder = false }
        )
    }
}
