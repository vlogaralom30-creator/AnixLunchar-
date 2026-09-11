package com.example.ui.components

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun LauncherGestureDetector(
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var totalDragY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Main content rendered first (LazyColumn handles vertical scrolling inside its bounds)
        content()

        // Wallpaper gesture area only on the right/empty side to avoid swallowing scroll gestures of the left app list
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            totalDragY = 0f
                        },
                        onDragEnd = {
                            if (totalDragY < -140f) {
                                onSwipeUp()
                            } else if (totalDragY > 140f) {
                                onSwipeDown()
                            }
                            totalDragY = 0f
                        },
                        onDragCancel = {
                            totalDragY = 0f
                        },
                        onVerticalDrag = { _, dragAmount ->
                            totalDragY += dragAmount
                        }
                    )
                }
        )
    }
}
