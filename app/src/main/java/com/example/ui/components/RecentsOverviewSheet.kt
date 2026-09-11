package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LauncherPreferences
import com.example.model.MemoryStatusInfo
import com.example.model.RecentAppItem
import com.example.model.RecentsOrientation
import com.example.util.SoundFeedbackUtil

@Composable
fun RecentsOverviewSheet(
    isOpen: Boolean,
    recentApps: List<RecentAppItem>,
    memoryInfo: MemoryStatusInfo,
    preferences: LauncherPreferences,
    onAppSelect: (RecentAppItem) -> Unit,
    onDismissApp: (RecentAppItem) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(tween(200)) + slideInVertically(
            animationSpec = tween(250, easing = FastOutSlowInEasing),
            initialOffsetY = { it / 3 }
        ),
        exit = fadeOut(tween(180)) + slideOutVertically(
            animationSpec = tween(220, easing = FastOutSlowInEasing),
            targetOffsetY = { it / 3 }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE60D1311))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("recents_overview_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar: Header & Dismiss
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Recent Apps",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (recentApps.isEmpty()) "No active tasks" else "${recentApps.size} apps in memory",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (preferences.enableClickSound) {
                                SoundFeedbackUtil.playClickSoundAndHaptic(context)
                            }
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0x22FFFFFF), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Recents",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Center Cards: Vertical or Horizontal list
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (recentApps.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "All background apps cleared",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else if (preferences.recentsOrientation == RecentsOrientation.HORIZONTAL) {
                        // Horizontal Card Deck
                        val listState = rememberLazyListState()
                        LazyRow(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            contentPadding = PaddingValues(horizontal = 36.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(
                                items = recentApps,
                                key = { it.packageName }
                            ) { appItem ->
                                RecentAppHorizontalCard(
                                    item = appItem,
                                    preferences = preferences,
                                    blurPreview = preferences.blurAppPreviews,
                                    onClick = {
                                        if (preferences.enableClickSound) {
                                            SoundFeedbackUtil.playClickSoundAndHaptic(context)
                                        }
                                        onAppSelect(appItem)
                                    },
                                    onDismiss = {
                                        if (preferences.enableClickSound) {
                                            SoundFeedbackUtil.playClickSoundAndHaptic(context)
                                        }
                                        onDismissApp(appItem)
                                    }
                                )
                            }
                        }
                    } else {
                        // Vertical Card Deck
                        val listState = rememberLazyListState()
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(
                                items = recentApps,
                                key = { it.packageName }
                            ) { appItem ->
                                RecentAppVerticalCard(
                                    item = appItem,
                                    preferences = preferences,
                                    blurPreview = preferences.blurAppPreviews,
                                    onClick = {
                                        if (preferences.enableClickSound) {
                                            SoundFeedbackUtil.playClickSoundAndHaptic(context)
                                        }
                                        onAppSelect(appItem)
                                    },
                                    onDismiss = {
                                        if (preferences.enableClickSound) {
                                            SoundFeedbackUtil.playClickSoundAndHaptic(context)
                                        }
                                        onDismissApp(appItem)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Controls: Memory RAM status & Clear All
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (preferences.showMemoryStatus) {
                        Surface(
                            color = Color(0x33283832),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Memory,
                                            contentDescription = "RAM",
                                            tint = Color(0xFF90B5A7),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Memory (RAM)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }

                                    Text(
                                        text = "${memoryInfo.availableGbString} / ${memoryInfo.totalGbString} free (${memoryInfo.percentFree}%)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF90B5A7)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val usedProgress = (100 - memoryInfo.percentFree) / 100f
                                LinearProgressIndicator(
                                    progress = { usedProgress.coerceIn(0.05f, 0.95f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF7FA89B),
                                    trackColor = Color(0x33FFFFFF)
                                )
                            }
                        }
                    }

                    if (recentApps.isNotEmpty()) {
                        Button(
                            onClick = {
                                if (preferences.enableClickSound) {
                                    SoundFeedbackUtil.playClickSoundAndHaptic(context)
                                }
                                onClearAll()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF436055),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("clear_all_recents_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ClearAll,
                                contentDescription = "Clear All",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Clear All Tasks",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentAppHorizontalCard(
    item: RecentAppItem,
    preferences: LauncherPreferences,
    blurPreview: Boolean,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight(0.85f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("recent_card_${item.packageName}"),
        color = Color(item.accentColor),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0x33FFFFFF)),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: App icon, Title, Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x33000000))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AppIconImage(
                        drawable = item.icon,
                        contentDescription = item.label,
                        packageName = item.packageName,
                        themed = preferences.themedIcons,
                        themeColor = Color(preferences.effectiveThemedIconColorHex),
                        iconStyle = preferences.themedIconStyle,
                        size = 24.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.label,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss app",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Window Preview Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .then(if (blurPreview) Modifier.blur(16.dp) else Modifier)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(item.accentColor).copy(alpha = 0.6f),
                                Color(0xFF101916)
                            )
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Realistic Android Card Mock Window Preview
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x22000000), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 80.dp, height = 12.dp)
                                .background(Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                        )
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color(0x33FFFFFF), CircleShape)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(10.dp)
                                .background(Color(0x22FFFFFF), RoundedCornerShape(5.dp))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(10.dp)
                                .background(Color(0x22FFFFFF), RoundedCornerShape(5.dp))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${item.memoryUsageMb} MB",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentAppVerticalCard(
    item: RecentAppItem,
    preferences: LauncherPreferences,
    blurPreview: Boolean,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("recent_card_${item.packageName}"),
        color = Color(item.accentColor),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0x33FFFFFF)),
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x33000000))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AppIconImage(
                        drawable = item.icon,
                        contentDescription = item.label,
                        packageName = item.packageName,
                        themed = preferences.themedIcons,
                        themeColor = Color(preferences.effectiveThemedIconColorHex),
                        iconStyle = preferences.themedIconStyle,
                        size = 24.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = item.label,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .then(if (blurPreview) Modifier.blur(16.dp) else Modifier)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(item.accentColor).copy(alpha = 0.6f),
                                Color(0xFF101916)
                            )
                        )
                    )
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x22000000), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(width = 100.dp, height = 10.dp)
                                .background(Color(0x33FFFFFF), RoundedCornerShape(5.dp))
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 160.dp, height = 8.dp)
                                .background(Color(0x22FFFFFF), RoundedCornerShape(4.dp))
                        )
                    }

                    Text(
                        text = "${item.memoryUsageMb} MB",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
