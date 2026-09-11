package com.example.ui.components

import android.content.Intent
import android.provider.MediaStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryStatusInfo
import com.example.model.ClockPosition
import com.example.model.LauncherPreferences
import com.example.model.LockNotificationItem
import com.example.model.LockShortcut
import com.example.util.SoundFeedbackUtil
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreenOverlay(
    isLocked: Boolean,
    preferences: LauncherPreferences,
    batteryInfo: BatteryStatusInfo,
    notifications: List<LockNotificationItem>,
    isNotificationListenerGranted: Boolean,
    isFlashlightOn: Boolean,
    currentTimeString: String,
    currentDateString: String,
    onUnlock: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onDismissNotification: (String) -> Unit,
    onTapNotification: (LockNotificationItem) -> Unit,
    onRequestNotificationAccess: () -> Unit
) {
    if (!isLocked || !preferences.lockScreenEnabled) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dragOffsetY = remember { Animatable(0f) }

    // Swipe up unlock threshold
    val unlockThreshold = -220f

    val animDuration = if (preferences.lockEnableAnimations) preferences.lockAnimationSpeedMs else 0

    val timeFormat = remember(preferences.lockClock24Hour, preferences.lockShowSeconds) {
        val pattern = when {
            preferences.lockClock24Hour && preferences.lockShowSeconds -> "HH:mm:ss"
            preferences.lockClock24Hour -> "HH:mm"
            preferences.lockShowSeconds -> "h:mm:ss a"
            else -> "h:mm"
        }
        SimpleDateFormat(pattern, Locale.getDefault())
    }

    val displayTime = remember(currentTimeString, preferences.lockClock24Hour, preferences.lockShowSeconds) {
        timeFormat.format(Calendar.getInstance().time)
    }

    val displayDate = remember(currentDateString) {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Calendar.getInstance().time)
    }

    val draggableState = rememberDraggableState { delta ->
        scope.launch {
            val newOffset = (dragOffsetY.value + delta).coerceAtMost(0f)
            dragOffsetY.snapTo(newOffset)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("nxv_lock_screen_overlay")
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStopped = { velocity ->
                    if (dragOffsetY.value < unlockThreshold || velocity < -1200f) {
                        SoundFeedbackUtil.playClickFeedback(context, preferences.enableSound, preferences.enableHaptic)
                        scope.launch {
                            dragOffsetY.animateTo(
                                targetValue = -1000f,
                                animationSpec = tween(animDuration, easing = FastOutSlowInEasing)
                            )
                            onUnlock()
                            dragOffsetY.snapTo(0f)
                        }
                    } else {
                        scope.launch {
                            dragOffsetY.animateTo(
                                targetValue = 0f,
                                animationSpec = spring()
                            )
                        }
                    }
                }
            )
            .offset { IntOffset(0, dragOffsetY.value.roundToInt()) }
    ) {
        // 1. Lock Screen Wallpaper Background
        val wallpaperRes = preferences.selectedWallpaper.drawableRes
        Image(
            painter = painterResource(id = wallpaperRes),
            contentDescription = "Lock Screen Wallpaper",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dim & Contrast Overlay
        val dimAlpha = (preferences.lockDimPercent / 100f).coerceIn(0f, 0.85f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = dimAlpha))
        )

        // Gradient top-down scrim for clock readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Gradient bottom-up scrim for notification/shortcut actions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.70f)
                        )
                    )
                )
        )

        // Lock Screen Main Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = when (preferences.lockClockPosition) {
                ClockPosition.TOP_LEFT -> Alignment.Start
                ClockPosition.TOP_CENTER -> Alignment.CenterHorizontally
                ClockPosition.TOP_RIGHT -> Alignment.End
            }
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Clock & Date Section
            val clockAlignment = when (preferences.lockClockPosition) {
                ClockPosition.TOP_LEFT -> Alignment.Start
                ClockPosition.TOP_CENTER -> Alignment.CenterHorizontally
                ClockPosition.TOP_RIGHT -> Alignment.End
            }

            Column(
                horizontalAlignment = clockAlignment,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secured Lock",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NXV SECURE",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Large Digital Clock
                Text(
                    text = displayTime,
                    color = Color.White,
                    fontSize = preferences.lockClockSizeSp.sp,
                    fontFamily = preferences.lockClockFontStyle.fontFamily,
                    fontWeight = preferences.lockClockFontStyle.fontWeight,
                    letterSpacing = preferences.lockClockFontStyle.letterSpacingSp.sp,
                    textAlign = when (preferences.lockClockPosition) {
                        ClockPosition.TOP_LEFT -> TextAlign.Start
                        ClockPosition.TOP_CENTER -> TextAlign.Center
                        ClockPosition.TOP_RIGHT -> TextAlign.End
                    },
                    modifier = Modifier.shadow(12.dp, spotColor = Color.Black)
                )

                if (preferences.lockShowDate) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = displayDate,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }

                // Battery Status & Weather Line
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                        contentDescription = "Battery Status",
                        tint = if (batteryInfo.isCharging) Color(0xFF7FA89B) else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${batteryInfo.levelPercent}%${if (batteryInfo.isCharging) " • Charging" else ""}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (preferences.lockShowWeather) {
                        Text(
                            text = "  •  ",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        )
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Weather",
                            tint = Color(0xFFF2D184),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "22°C Clear",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Notifications List Section
            if (preferences.lockShowNotifications) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Notifications",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (preferences.lockShowNotificationCount && notifications.isNotEmpty()) {
                            Surface(
                                color = Color(0x33FFFFFF),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "${notifications.size}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!isNotificationListenerGranted) {
                        // Permission Access Banner Card
                        Surface(
                            color = Color(0x351F2A27),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x447FA89B)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRequestNotificationAccess() }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = Color(0xFF7FA89B),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Notification Access Required",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Tap to grant permission to view live notifications on lock screen",
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (notifications.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No new notifications",
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = notifications,
                                key = { it.id }
                            ) { notif ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { dismissValue ->
                                        if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                            onDismissNotification(notif.id)
                                            true
                                        } else false
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color(0x33FF5252))
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Text(
                                                text = "Dismiss",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    },
                                    content = {
                                        Surface(
                                            color = Color(0x33141918),
                                            shape = RoundedCornerShape(16.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onTapNotification(notif)
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (notif.appIconDrawable != null) {
                                                    Image(
                                                        painter = rememberDrawablePainter(notif.appIconDrawable),
                                                        contentDescription = notif.appName,
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF4C6B61)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = notif.appName.take(1).uppercase(),
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 16.sp
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = notif.appName,
                                                            color = Color.White.copy(alpha = 0.9f),
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        Text(
                                                            text = notif.timeFormatted,
                                                            color = Color.White.copy(alpha = 0.5f),
                                                            fontSize = 11.sp
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.height(2.dp))

                                                    if (preferences.lockShowNotificationContent) {
                                                        val titleText = if (preferences.lockHideSensitiveNotifications && notif.isSensitive) {
                                                            "New Notification"
                                                        } else notif.title

                                                        val bodyText = if (preferences.lockHideSensitiveNotifications && notif.isSensitive) {
                                                            "Notification content hidden"
                                                        } else notif.text

                                                        if (titleText.isNotBlank()) {
                                                            Text(
                                                                text = titleText,
                                                                color = Color.White,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                        if (bodyText.isNotBlank()) {
                                                            Text(
                                                                text = bodyText,
                                                                color = Color.White.copy(alpha = 0.75f),
                                                                fontSize = 12.sp,
                                                                maxLines = 2,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    } else {
                                                        Text(
                                                            text = "Contents hidden",
                                                            color = Color.White.copy(alpha = 0.6f),
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Bottom Actions & Swipe Up Unlock Prompt
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Swipe Up Prompt Indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            SoundFeedbackUtil.playClickFeedback(context, preferences.enableSound, preferences.enableHaptic)
                            onUnlock()
                        }
                        .padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Swipe up to unlock",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Swipe up to unlock",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Shortcut Button
                    LockShortcutButton(
                        shortcut = preferences.lockLeftShortcut,
                        isFlashlightActive = isFlashlightOn,
                        onAction = {
                            handleShortcutAction(
                                shortcut = preferences.lockLeftShortcut,
                                context = context,
                                onToggleFlashlight = onToggleFlashlight,
                                onUnlock = onUnlock
                            )
                        }
                    )

                    // Right Shortcut Button
                    LockShortcutButton(
                        shortcut = preferences.lockRightShortcut,
                        isFlashlightActive = isFlashlightOn,
                        onAction = {
                            handleShortcutAction(
                                shortcut = preferences.lockRightShortcut,
                                context = context,
                                onToggleFlashlight = onToggleFlashlight,
                                onUnlock = onUnlock
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LockShortcutButton(
    shortcut: LockShortcut,
    isFlashlightActive: Boolean,
    onAction: () -> Unit
) {
    if (shortcut == LockShortcut.NONE) {
        Spacer(modifier = Modifier.size(52.dp))
        return
    }

    val iconVector = when (shortcut) {
        LockShortcut.FLASHLIGHT -> if (isFlashlightActive) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff
        LockShortcut.CAMERA -> Icons.Default.CameraAlt
        LockShortcut.PHONE -> Icons.Default.Call
        LockShortcut.SEARCH -> Icons.Default.Search
        LockShortcut.NONE -> Icons.Default.Lock
    }

    Surface(
        onClick = onAction,
        shape = CircleShape,
        color = if (shortcut == LockShortcut.FLASHLIGHT && isFlashlightActive) Color(0xFF7FA89B) else Color(0x33FFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF)),
        modifier = Modifier.size(52.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = iconVector,
                contentDescription = shortcut.displayName,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun handleShortcutAction(
    shortcut: LockShortcut,
    context: android.content.Context,
    onToggleFlashlight: () -> Unit,
    onUnlock: () -> Unit
) {
    SoundFeedbackUtil.playClickHaptic(context)
    when (shortcut) {
        LockShortcut.FLASHLIGHT -> {
            onToggleFlashlight()
        }
        LockShortcut.CAMERA -> {
            try {
                val cameraIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(cameraIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        LockShortcut.PHONE -> {
            try {
                val phoneIntent = Intent(Intent.ACTION_DIAL).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(phoneIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        LockShortcut.SEARCH -> {
            onUnlock()
        }
        LockShortcut.NONE -> {}
    }
}

@Composable
private fun rememberDrawablePainter(drawable: android.graphics.drawable.Drawable?): androidx.compose.ui.graphics.painter.Painter {
    return remember(drawable) {
        object : androidx.compose.ui.graphics.painter.Painter() {
            override val intrinsicSize: androidx.compose.ui.geometry.Size
                get() = if (drawable != null && drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                    androidx.compose.ui.geometry.Size(
                        drawable.intrinsicWidth.toFloat(),
                        drawable.intrinsicHeight.toFloat()
                    )
                } else {
                    androidx.compose.ui.geometry.Size(48f, 48f)
                }

            override fun androidx.compose.ui.graphics.drawscope.DrawScope.onDraw() {
                drawable?.let { d ->
                    d.setBounds(0, 0, size.width.toInt(), size.height.toInt())
                    d.draw(drawContext.canvas.nativeCanvas)
                }
            }
        }
    }
}
