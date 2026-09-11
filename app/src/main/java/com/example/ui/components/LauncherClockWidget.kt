package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClockFontStyle
import com.example.model.ClockPosition

@Composable
fun LauncherClockWidget(
    timeString: String,
    dateString: String,
    weatherTempString: String,
    clockSizeSp: Int,
    clockPosition: ClockPosition,
    clockFontStyle: ClockFontStyle = ClockFontStyle.MINIMAL_LIGHT,
    showWeather: Boolean,
    onClockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alignment = when (clockPosition) {
        ClockPosition.TOP_LEFT -> Alignment.Start
        ClockPosition.TOP_CENTER -> Alignment.CenterHorizontally
        ClockPosition.TOP_RIGHT -> Alignment.End
    }

    val textShadow = Shadow(
        color = Color(0x99000000),
        offset = Offset(0f, 4f),
        blurRadius = 8f
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClockClick
            ),
        horizontalAlignment = alignment
    ) {
        // Large digital clock using selected minimalist clock font style
        Text(
            text = timeString,
            style = TextStyle(
                fontFamily = clockFontStyle.fontFamily,
                fontWeight = clockFontStyle.fontWeight,
                fontSize = clockSizeSp.sp,
                letterSpacing = clockFontStyle.letterSpacingSp.sp,
                color = Color.White,
                shadow = textShadow
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Date and optional weather line
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = dateString,
                style = TextStyle(
                    fontFamily = clockFontStyle.fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    shadow = textShadow
                )
            )

            if (showWeather) {
                Spacer(modifier = Modifier.width(10.dp))

                Icon(
                    imageVector = Icons.Outlined.WbSunny,
                    contentDescription = "Weather",
                    tint = Color(0xFFF9D776),
                    modifier = Modifier.height(18.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = weatherTempString,
                    style = TextStyle(
                        fontFamily = clockFontStyle.fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        shadow = textShadow
                    )
                )
            }
        }
    }
}
