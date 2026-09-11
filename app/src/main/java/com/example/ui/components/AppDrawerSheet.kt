package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSortOrder
import com.example.model.InstalledApp
import com.example.model.LauncherPreferences
import com.example.util.SoundFeedbackUtil
import kotlinx.coroutines.launch

@Composable
fun AppDrawerSheet(
    isOpen: Boolean,
    apps: List<InstalledApp>,
    searchQuery: String,
    preferences: LauncherPreferences? = null,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: ((AppSortOrder) -> Unit)? = null,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit,
    onOpenSettings: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(animationSpec = tween(220)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(200, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(180))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101413))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Don't dismiss when tapping content
                    )
            ) {
                // Header search bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("app_drawer_search_field"),
                        placeholder = {
                            Text(
                                "Search installed apps...",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0x33FFFFFF),
                            unfocusedContainerColor = Color(0x22FFFFFF),
                            focusedBorderColor = Color(0xFF7FA89B),
                            unfocusedBorderColor = Color(0x22FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (onOpenSettings != null) {
                        IconButton(
                            onClick = {
                                onDismiss()
                                onOpenSettings()
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0x22FFFFFF), CircleShape)
                                .testTag("app_drawer_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Launcher Settings",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0x22FFFFFF), CircleShape)
                            .testTag("app_drawer_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Sort Options Chips
                if (onSortOrderChange != null && preferences != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sort:",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(end = 2.dp)
                        )

                        AppSortOrder.values().forEach { order ->
                            val isSelected = preferences.appSortOrder == order
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) Color(0xFF6F8E83) else Color(0x1AFFFFFF),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onSortOrderChange(order) }
                                    .testTag("sort_chip_${order.name.lowercase()}")
                            ) {
                                Text(
                                    text = order.displayName,
                                    color = if (isSelected) Color(0xFF101915) else Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val gridState = rememberLazyGridState()

                val scrollIntensity by animateFloatAsState(
                    targetValue = if (gridState.isScrollInProgress) 1f else 0f,
                    animationSpec = if (gridState.isScrollInProgress) {
                        tween(durationMillis = 180, easing = FastOutSlowInEasing)
                    } else {
                        tween(durationMillis = 350, easing = FastOutSlowInEasing)
                    },
                    label = "drawer_grid_scroll_intensity"
                )

                // App Grid (4 columns)
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = apps,
                        key = { _, app -> app.packageName }
                    ) { index, app ->
                        DrawerGridAppItem(
                            app = app,
                            index = index,
                            gridState = gridState,
                            scrollIntensity = scrollIntensity,
                            preferences = preferences,
                            onClick = { onAppClick(app) },
                            onLongClick = { onAppLongClick(app) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawerGridAppItem(
    app: InstalledApp,
    index: Int = 0,
    gridState: LazyGridState? = null,
    scrollIntensity: Float = 0f,
    preferences: LauncherPreferences? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "grid_item_scale"
    )

    val launchAnimScale = remember { Animatable(1f) }
    val launchAnimAlpha = remember { Animatable(1f) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .graphicsLayer {
                var dynamicScale = 1f
                var dynamicAlpha = 1f

                if (preferences?.enableFisheyeScroll == true && gridState != null && scrollIntensity > 0.001f) {
                    val itemInfo = gridState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                    val viewportHeight = gridState.layoutInfo.viewportSize.height.toFloat()

                    if (itemInfo != null && viewportHeight > 0f) {
                        val viewportCenter = viewportHeight / 2f
                        val itemCenter = itemInfo.offset.y + (itemInfo.size.height / 2f)
                        val distanceFromCenter = kotlin.math.abs(viewportCenter - itemCenter)
                        val maxDistance = (viewportHeight / 2f).coerceAtLeast(1f)
                        val normalizedDistance = (distanceFromCenter / maxDistance).coerceIn(0f, 1f)
                        val curveFactor = (kotlin.math.cos(normalizedDistance * Math.PI.toFloat()) + 1f) / 2f

                        val minScale = 0.82f
                        val maxScale = 1.15f
                        val targetScale = minScale + (maxScale - minScale) * curveFactor
                        val targetAlpha = 0.60f + (0.40f * curveFactor)

                        // Blend between resting (1.0f) and active scroll magnification
                        dynamicScale = 1f + (targetScale - 1f) * scrollIntensity
                        dynamicAlpha = 1f + (targetAlpha - 1f) * scrollIntensity
                    }
                }

                scaleX = dynamicScale * scale * launchAnimScale.value
                scaleY = dynamicScale * scale * launchAnimScale.value
                alpha = dynamicAlpha * launchAnimAlpha.value
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (preferences != null) {
                        SoundFeedbackUtil.playClickFeedback(context, preferences.enableSound, preferences.enableHaptic)
                    }
                    coroutineScope.launch {
                        launchAnimScale.animateTo(
                            1.2f,
                            animationSpec = tween(120, easing = FastOutSlowInEasing)
                        )
                        launchAnimAlpha.animateTo(0.6f, animationSpec = tween(80))
                        onClick()
                        launchAnimScale.snapTo(1f)
                        launchAnimAlpha.snapTo(1f)
                    }
                },
                onLongClick = {
                    if (preferences != null) {
                        SoundFeedbackUtil.playClickFeedback(context, false, preferences.enableHaptic)
                    }
                    onLongClick()
                }
            )
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag("drawer_app_${app.packageName}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppIconImage(
            drawable = app.icon,
            contentDescription = app.displayName,
            packageName = app.packageName,
            themed = preferences?.themedIcons ?: false,
            themeColor = if (preferences != null) Color(preferences.effectiveThemedIconColorHex) else Color(0xFF141918),
            iconStyle = preferences?.themedIconStyle ?: com.example.model.ThemedIconStyle.SMART_MINIMAL,
            size = 48.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = app.displayName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = preferences?.clockFontStyle?.fontFamily,
            color = Color.White.copy(alpha = 0.95f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
