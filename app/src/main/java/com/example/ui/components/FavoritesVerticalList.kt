package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InstalledApp
import com.example.model.LauncherPreferences
import com.example.util.SoundFeedbackUtil
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun FavoritesVerticalList(
    apps: List<InstalledApp>,
    preferences: LauncherPreferences,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Play subtle mechanical scroll tick feedback when scrolling items up & down
    if (preferences.enableClickSound) {
        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .distinctUntilChanged()
                .collect {
                    SoundFeedbackUtil.playScrollTick(context)
                }
        }
    }

    // Smoothly animate fisheye intensity:
    // When idle (not scrolling) -> 0f (all items uniform 1.0 standard size 1111)
    // When scrolling -> 1f (dynamic 1, 2, 3, 4, 3, 2, 1 magnifying lens effect)
    val scrollIntensity by animateFloatAsState(
        targetValue = if (listState.isScrollInProgress) 1f else 0f,
        animationSpec = if (listState.isScrollInProgress) {
            tween(durationMillis = 180, easing = FastOutSlowInEasing)
        } else {
            tween(durationMillis = 350, easing = FastOutSlowInEasing)
        },
        label = "fisheye_scroll_intensity"
    )

    // Generous top/bottom padding ensures that top and bottom items can both scroll directly into center focus
    val verticalPadding = if (preferences.enableFisheyeScroll) {
        PaddingValues(top = 140.dp, bottom = 180.dp)
    } else {
        PaddingValues(top = 16.dp, bottom = 80.dp)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(0.72f),
        contentPadding = verticalPadding,
        verticalArrangement = Arrangement.spacedBy(preferences.itemSpacingDp.dp)
    ) {
        itemsIndexed(
            items = apps,
            key = { _, app -> app.packageName }
        ) { index, app ->
            FavoriteAppItem(
                app = app,
                index = index,
                listState = listState,
                scrollIntensity = scrollIntensity,
                preferences = preferences,
                onClick = {
                    if (preferences.enableClickSound) {
                        SoundFeedbackUtil.playClickSoundAndHaptic(context)
                    }
                    onAppClick(app)
                },
                onLongClick = {
                    if (preferences.enableClickSound) {
                        SoundFeedbackUtil.playClickSoundAndHaptic(context)
                    }
                    onAppLongClick(app)
                }
            )
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
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()

    // Tactile bounce when pressed
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
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                // Anchor on left edge so item expands outwards to the right and vertically from center
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

                        // Cosine lens curve:
                        // Center distance 0 -> curveFactor 1.0 (Maximum magnification)
                        // Edge distance 1 -> curveFactor 0.0 (Minimum scale)
                        // Resulting curve: 1, 2, 3, 4, 3, 2, 1
                        // _ (top: small)
                        // __ (mid: medium)
                        // ___ (center: BIG)
                        // __ (mid: medium)
                        // _ (bottom: small)
                        val curveFactor = (kotlin.math.cos(normalizedDistance * Math.PI.toFloat()) + 1f) / 2f

                        val minScale = 0.70f
                        val maxScale = preferences.fisheyeMagnification
                        val targetScale = minScale + (maxScale - minScale) * curveFactor

                        // Alpha curve: 0.50f at extremes to 1.0f in center focus
                        val targetAlpha = 0.50f + (0.50f * curveFactor)

                        // Smooth horizontal wheel/lens arc to the right
                        val targetTranslationX = curveFactor * 22f

                        // Blend between resting standard (1, 1, 1, 1) and active scrolling (1, 2, 3, 4, 3, 2, 1)
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
                        // Smooth launch pulse animation
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
                contentDescription = app.label,
                packageName = app.packageName,
                themed = preferences.themedIcons,
                themeColor = Color(preferences.themedIconColor.colorHex),
                iconStyle = preferences.themedIconStyle,
                size = preferences.iconSizeDp.dp
            )

            if (preferences.showAppNames) {
                Spacer(modifier = Modifier.width(18.dp))

                Text(
                    text = app.label,
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
