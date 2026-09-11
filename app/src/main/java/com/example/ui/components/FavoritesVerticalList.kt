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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
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

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(0.72f),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(preferences.itemSpacingDp.dp)
    ) {
        items(
            items = apps,
            key = { it.packageName }
        ) { app ->
            FavoriteAppItem(
                app = app,
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
    preferences: LauncherPreferences,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()

    // Smooth fluid tactile spring scale when tapped
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "item_scale"
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
            .scale(scale)
            .graphicsLayer {
                scaleX = launchAnimScale.value
                scaleY = launchAnimScale.value
                alpha = launchAnimAlpha.value
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
