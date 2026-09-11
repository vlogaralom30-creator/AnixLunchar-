package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.model.AppFolder
import com.example.model.HomeItem
import com.example.model.InstalledApp
import com.example.model.LauncherPreferences
import com.example.util.SoundFeedbackUtil
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeGridList(
    homeItems: List<HomeItem>,
    preferences: LauncherPreferences,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit,
    onFolderClick: (AppFolder) -> Unit,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onMergeAppsIntoFolder: (fromApp: InstalledApp, targetApp: InstalledApp) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gridState = rememberLazyGridState()
    val gridLayout = preferences.homeGridLayout
    val columns = gridLayout.columns

    // Drag and drop state
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var hoverIndex by remember { mutableIntStateOf(-1) }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val calculatedIconSize = when {
            columns >= 6 -> min(preferences.iconSizeDp, 40).dp
            columns == 5 -> min(preferences.iconSizeDp, 46).dp
            else -> min(preferences.iconSizeDp, 52).dp
        }

        val horizontalSpacing = when {
            columns >= 6 -> 4.dp
            columns == 5 -> 8.dp
            else -> 12.dp
        }

        val verticalSpacing = when {
            gridLayout.rows >= 7 -> 10.dp
            gridLayout.rows == 6 -> 14.dp
            else -> 18.dp
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = 8.dp,
                bottom = 40.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing)
        ) {
            itemsIndexed(
                items = homeItems,
                key = { _, item -> item.id }
            ) { index, item ->
                val isBeingDragged = draggedIndex == index
                val isHoverTarget = hoverIndex == index && draggedIndex != -1 && draggedIndex != index

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(if (isBeingDragged) 10f else 1f)
                        .graphicsLayer {
                            if (isBeingDragged) {
                                translationX = dragOffsetX
                                translationY = dragOffsetY
                                scaleX = 1.12f
                                scaleY = 1.12f
                                shadowElevation = 16f
                            } else if (isHoverTarget) {
                                scaleX = 1.05f
                                scaleY = 1.05f
                            }
                        }
                        .pointerInput(preferences.lockHomeScreenLayout, homeItems) {
                            if (!preferences.lockHomeScreenLayout) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedIndex = index
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                        hoverIndex = -1
                                        SoundFeedbackUtil.playClickFeedback(context, false, preferences.enableHaptic)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetX += dragAmount.x
                                        dragOffsetY += dragAmount.y

                                        // Estimate hovered slot index
                                        val layoutInfo = gridState.layoutInfo
                                        val draggedCenter = Offset(
                                            change.position.x + dragOffsetX,
                                            change.position.y + dragOffsetY
                                        )

                                        var bestMatchIndex = -1
                                        var bestDistance = Float.MAX_VALUE
                                        layoutInfo.visibleItemsInfo.forEach { visibleItem ->
                                            if (visibleItem.index != index && visibleItem.index < homeItems.size) {
                                                val itemCenterX = visibleItem.offset.x + visibleItem.size.width / 2f
                                                val itemCenterY = visibleItem.offset.y + visibleItem.size.height / 2f
                                                val dist = (draggedCenter.x - itemCenterX) * (draggedCenter.x - itemCenterX) +
                                                        (draggedCenter.y - itemCenterY) * (draggedCenter.y - itemCenterY)
                                                if (dist < 8000f && dist < bestDistance) {
                                                    bestDistance = dist
                                                    bestMatchIndex = visibleItem.index
                                                }
                                            }
                                        }
                                        hoverIndex = bestMatchIndex
                                    },
                                    onDragEnd = {
                                        val fromIdx = draggedIndex
                                        val toIdx = hoverIndex
                                        if (fromIdx in homeItems.indices && toIdx in homeItems.indices && fromIdx != toIdx) {
                                            val fromItem = homeItems[fromIdx]
                                            val targetItem = homeItems[toIdx]

                                            // If dropping an app onto another app -> merge into folder
                                            if (fromItem is HomeItem.App && targetItem is HomeItem.App) {
                                                onMergeAppsIntoFolder(fromItem.app, targetItem.app)
                                                SoundFeedbackUtil.playClickFeedback(context, preferences.enableSound, preferences.enableHaptic)
                                            } else {
                                                // Reorder positions
                                                onReorder(fromIdx, toIdx)
                                                SoundFeedbackUtil.playClickFeedback(context, preferences.enableSound, preferences.enableHaptic)
                                            }
                                        }
                                        draggedIndex = -1
                                        hoverIndex = -1
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                    },
                                    onDragCancel = {
                                        draggedIndex = -1
                                        hoverIndex = -1
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                    }
                                )
                            }
                        }
                ) {
                    when (item) {
                        is HomeItem.App -> {
                            HomeGridAppItem(
                                app = item.app,
                                preferences = preferences,
                                iconSize = calculatedIconSize,
                                isHovered = isHoverTarget,
                                onAppClick = {
                                    SoundFeedbackUtil.playClickFeedback(context, preferences.enableSound, preferences.enableHaptic)
                                    onAppClick(item.app)
                                },
                                onAppLongClick = {
                                    SoundFeedbackUtil.playClickFeedback(context, preferences.enableSound, preferences.enableHaptic)
                                    onAppLongClick(item.app)
                                }
                            )
                        }
                        is HomeItem.Folder -> {
                            HomeGridFolderItem(
                                folder = item.folder,
                                apps = item.apps,
                                preferences = preferences,
                                iconSize = calculatedIconSize,
                                isHovered = isHoverTarget,
                                onFolderClick = {
                                    SoundFeedbackUtil.playClickFeedback(context, preferences.enableSound, preferences.enableHaptic)
                                    onFolderClick(item.folder)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeGridAppItem(
    app: InstalledApp,
    preferences: LauncherPreferences,
    iconSize: androidx.compose.ui.unit.Dp,
    isHovered: Boolean = false,
    onAppClick: () -> Unit,
    onAppLongClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
        label = "grid_item_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isHovered) Color(0x336F8E83) else Color.Transparent)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White.copy(alpha = 0.2f), bounded = true),
                onClick = onAppClick,
                onLongClick = onAppLongClick
            )
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .testTag("home_grid_item_${app.packageName}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppIconImage(
            drawable = app.icon,
            contentDescription = app.displayName,
            packageName = app.packageName,
            themed = preferences.themedIcons,
            themeColor = Color(preferences.effectiveThemedIconColorHex),
            iconStyle = preferences.themedIconStyle,
            size = iconSize
        )

        if (preferences.showAppNames) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.displayName,
                color = Color.White,
                fontSize = if (preferences.homeGridLayout.columns >= 6) 11.sp else 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.8f),
                        offset = Offset(0f, 2f),
                        blurRadius = 4f
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeGridFolderItem(
    folder: AppFolder,
    apps: List<InstalledApp>,
    preferences: LauncherPreferences,
    iconSize: androidx.compose.ui.unit.Dp,
    isHovered: Boolean = false,
    onFolderClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
        label = "folder_item_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isHovered) Color(0x446F8E83) else Color.Transparent)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White.copy(alpha = 0.2f), bounded = true),
                onClick = onFolderClick
            )
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .testTag("home_grid_folder_${folder.id}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Folder Icon Mini Grid Container
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x38FFFFFF))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            val previewApps = apps.take(4)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    previewApps.getOrNull(0)?.let { app ->
                        AppIconImage(drawable = app.icon, contentDescription = null, size = (iconSize.value * 0.38f).dp)
                    }
                    previewApps.getOrNull(1)?.let { app ->
                        AppIconImage(drawable = app.icon, contentDescription = null, size = (iconSize.value * 0.38f).dp)
                    }
                }
                if (previewApps.size > 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        previewApps.getOrNull(2)?.let { app ->
                            AppIconImage(drawable = app.icon, contentDescription = null, size = (iconSize.value * 0.38f).dp)
                        }
                        previewApps.getOrNull(3)?.let { app ->
                            AppIconImage(drawable = app.icon, contentDescription = null, size = (iconSize.value * 0.38f).dp)
                        }
                    }
                }
            }
        }

        if (preferences.showAppNames) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = folder.name,
                color = Color.White,
                fontSize = if (preferences.homeGridLayout.columns >= 6) 11.sp else 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.8f),
                        offset = Offset(0f, 2f),
                        blurRadius = 4f
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
