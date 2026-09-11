package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.model.AppFolder
import com.example.model.HomeItem
import com.example.model.InstalledApp
import com.example.model.LauncherPreferences
import com.example.util.SoundFeedbackUtil
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun FavoritesVerticalList(
    homeItems: List<HomeItem>,
    preferences: LauncherPreferences,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit,
    onFolderClick: (AppFolder) -> Unit,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onMergeAppsIntoFolder: (fromApp: InstalledApp, targetApp: InstalledApp) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current

    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var hoverIndex by remember { mutableIntStateOf(-1) }

    if (preferences.enableSound || preferences.enableHaptic) {
        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .distinctUntilChanged()
                .collect {
                    SoundFeedbackUtil.playScrollTickFeedback(context, preferences.enableSound, preferences.enableHaptic)
                }
        }
    }

    val scrollIntensity by animateFloatAsState(
        targetValue = if (listState.isScrollInProgress) 1f else 0f,
        animationSpec = if (listState.isScrollInProgress) {
            tween(durationMillis = 180, easing = FastOutSlowInEasing)
        } else {
            tween(durationMillis = 350, easing = FastOutSlowInEasing)
        },
        label = "fisheye_scroll_intensity"
    )

    val verticalPadding = if (preferences.enableFisheyeScroll) {
        PaddingValues(top = 140.dp, bottom = 180.dp)
    } else {
        PaddingValues(top = 16.dp, bottom = 80.dp)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(0.76f),
        contentPadding = verticalPadding,
        verticalArrangement = Arrangement.spacedBy(preferences.itemSpacingDp.dp)
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
                            translationY = dragOffsetY
                            scaleX = 1.08f
                            scaleY = 1.08f
                            shadowElevation = 16f
                        }
                    }
                    .pointerInput(preferences.lockHomeScreenLayout, homeItems) {
                        if (!preferences.lockHomeScreenLayout) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggedIndex = index
                                    dragOffsetY = 0f
                                    hoverIndex = -1
                                    SoundFeedbackUtil.playClickFeedback(context, false, preferences.enableHaptic)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffsetY += dragAmount.y

                                    val layoutInfo = listState.layoutInfo
                                    val draggedCenterY = change.position.y + dragOffsetY

                                    var bestMatchIndex = -1
                                    var bestDistance = Float.MAX_VALUE
                                    layoutInfo.visibleItemsInfo.forEach { visibleItem ->
                                        if (visibleItem.index != index && visibleItem.index < homeItems.size) {
                                            val itemCenterY = visibleItem.offset + visibleItem.size / 2f
                                            val dist = kotlin.math.abs(draggedCenterY - itemCenterY)
                                            if (dist < 120f && dist < bestDistance) {
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

                                        if (fromItem is HomeItem.App && targetItem is HomeItem.App) {
                                            onMergeAppsIntoFolder(fromItem.app, targetItem.app)
                                            SoundFeedbackUtil.playClickFeedback(context, preferences.enableSound, preferences.enableHaptic)
                                        } else {
                                            onReorder(fromIdx, toIdx)
                                            SoundFeedbackUtil.playClickFeedback(context, preferences.enableSound, preferences.enableHaptic)
                                        }
                                    }
                                    draggedIndex = -1
                                    hoverIndex = -1
                                    dragOffsetY = 0f
                                },
                                onDragCancel = {
                                    draggedIndex = -1
                                    hoverIndex = -1
                                    dragOffsetY = 0f
                                }
                            )
                        }
                    }
            ) {
                when (item) {
                    is HomeItem.App -> {
                        FavoriteAppItem(
                            app = item.app,
                            index = index,
                            listState = listState,
                            scrollIntensity = scrollIntensity,
                            preferences = preferences,
                            isHovered = isHoverTarget,
                            onClick = {
                                SoundFeedbackUtil.playClickFeedback(context, preferences.enableSound, preferences.enableHaptic)
                                onAppClick(item.app)
                            },
                            onLongClick = {
                                SoundFeedbackUtil.playClickFeedback(context, preferences.enableSound, preferences.enableHaptic)
                                onAppLongClick(item.app)
                            }
                        )
                    }
                    is HomeItem.Folder -> {
                        FavoriteFolderItem(
                            folder = item.folder,
                            apps = item.apps,
                            index = index,
                            listState = listState,
                            scrollIntensity = scrollIntensity,
                            preferences = preferences,
                            isHovered = isHoverTarget,
                            onClick = {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteAppItem(
    app: InstalledApp,
    index: Int,
    listState: LazyListState,
    scrollIntensity: Float = 0f,
    preferences: LauncherPreferences,
    isHovered: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "item_press_scale"
    )

    val launchAnimScale = remember { Animatable(1f) }
    val launchAnimAlpha = remember { Animatable(1f) }

    val textShadow = Shadow(
        color = Color(0xCC000000),
        offset = Offset(0f, 3f),
        blurRadius = 6f
    )

    Surface(
        color = if (isHovered) Color(0x336F8E83) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                transformOrigin = TransformOrigin(0f, 0.5f)

                var dynamicScale = 1f
                var dynamicAlpha = 1f
                var dynamicTranslationX = 0f

                if (preferences.enableFisheyeScroll && scrollIntensity > 0.001f) {
                    val itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                    val viewportHeight = listState.layoutInfo.viewportSize.height.toFloat()

                    if (itemInfo != null && viewportHeight > 0f) {
                        val viewportCenter = viewportHeight / 2f
                        val itemCenter = itemInfo.offset + (itemInfo.size / 2f)
                        val distanceFromCenter = kotlin.math.abs(viewportCenter - itemCenter)
                        val maxDistance = (viewportHeight / 2f).coerceAtLeast(1f)
                        val normalizedDistance = (distanceFromCenter / maxDistance).coerceIn(0f, 1f)

                        val curveFactor = (kotlin.math.cos(normalizedDistance * Math.PI.toFloat()) + 1f) / 2f

                        val minScale = 0.70f
                        val maxScale = preferences.fisheyeMagnification
                        val targetScale = minScale + (maxScale - minScale) * curveFactor

                        val targetAlpha = 0.50f + (0.50f * curveFactor)
                        val targetTranslationX = curveFactor * 22f

                        dynamicScale = 1f + (targetScale - 1f) * scrollIntensity
                        dynamicAlpha = 1f + (targetAlpha - 1f) * scrollIntensity
                        dynamicTranslationX = targetTranslationX * scrollIntensity
                    }
                }

                scaleX = dynamicScale * pressScale * launchAnimScale.value
                scaleY = dynamicScale * pressScale * launchAnimScale.value
                alpha = dynamicAlpha * launchAnimAlpha.value
                translationX = dynamicTranslationX
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.35f)),
                onClick = {
                    coroutineScope.launch {
                        launchAnimScale.animateTo(
                            1.15f,
                            animationSpec = tween(120, easing = FastOutSlowInEasing)
                        )
                        launchAnimAlpha.animateTo(
                            0.7f,
                            animationSpec = tween(80)
                        )
                        onClick()
                        launchAnimScale.snapTo(1f)
                        launchAnimAlpha.snapTo(1f)
                    }
                },
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
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
                Spacer(modifier = Modifier.width(18.dp))

                Text(
                    text = app.displayName,
                    style = TextStyle(
                        fontFamily = preferences.clockFontStyle.fontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 17.sp,
                        letterSpacing = 0.4.sp,
                        color = Color.White,
                        shadow = textShadow
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteFolderItem(
    folder: AppFolder,
    apps: List<InstalledApp>,
    index: Int,
    listState: LazyListState,
    scrollIntensity: Float = 0f,
    preferences: LauncherPreferences,
    isHovered: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "folder_press_scale"
    )

    val textShadow = Shadow(
        color = Color(0xCC000000),
        offset = Offset(0f, 3f),
        blurRadius = 6f
    )

    val iconSize = preferences.iconSizeDp.dp

    Surface(
        color = if (isHovered) Color(0x446F8E83) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                transformOrigin = TransformOrigin(0f, 0.5f)

                var dynamicScale = 1f
                var dynamicAlpha = 1f
                var dynamicTranslationX = 0f

                if (preferences.enableFisheyeScroll && scrollIntensity > 0.001f) {
                    val itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                    val viewportHeight = listState.layoutInfo.viewportSize.height.toFloat()

                    if (itemInfo != null && viewportHeight > 0f) {
                        val viewportCenter = viewportHeight / 2f
                        val itemCenter = itemInfo.offset + (itemInfo.size / 2f)
                        val distanceFromCenter = kotlin.math.abs(viewportCenter - itemCenter)
                        val maxDistance = (viewportHeight / 2f).coerceAtLeast(1f)
                        val normalizedDistance = (distanceFromCenter / maxDistance).coerceIn(0f, 1f)

                        val curveFactor = (kotlin.math.cos(normalizedDistance * Math.PI.toFloat()) + 1f) / 2f

                        val minScale = 0.70f
                        val maxScale = preferences.fisheyeMagnification
                        val targetScale = minScale + (maxScale - minScale) * curveFactor

                        val targetAlpha = 0.50f + (0.50f * curveFactor)
                        val targetTranslationX = curveFactor * 22f

                        dynamicScale = 1f + (targetScale - 1f) * scrollIntensity
                        dynamicAlpha = 1f + (targetAlpha - 1f) * scrollIntensity
                        dynamicTranslationX = targetTranslationX * scrollIntensity
                    }
                }

                scaleX = dynamicScale * pressScale
                scaleY = dynamicScale * pressScale
                alpha = dynamicAlpha
                translationX = dynamicTranslationX
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.35f)),
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Folder Icon Container
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
                Spacer(modifier = Modifier.width(18.dp))

                Column {
                    Text(
                        text = folder.name,
                        style = TextStyle(
                            fontFamily = preferences.clockFontStyle.fontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 17.sp,
                            letterSpacing = 0.4.sp,
                            color = Color.White,
                            shadow = textShadow
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${apps.size} apps",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
