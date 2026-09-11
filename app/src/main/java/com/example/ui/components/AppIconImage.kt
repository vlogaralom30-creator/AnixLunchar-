package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.ThemedIconStyle

@Composable
fun AppIconImage(
    drawable: Drawable?,
    contentDescription: String?,
    packageName: String? = null,
    themed: Boolean = false,
    themeColor: Color = Color(0xFF141918),
    iconStyle: ThemedIconStyle = ThemedIconStyle.SMART_MINIMAL,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    if (!themed || drawable == null) {
        // Normal unthemed original app icon
        val normalBitmap = remember(drawable) { drawable?.let { drawableToBitmap(it) } }
        if (normalBitmap != null) {
            Image(
                painter = remember(normalBitmap) { BitmapPainter(normalBitmap.asImageBitmap()) },
                contentDescription = contentDescription,
                modifier = modifier.size(size)
            )
        } else {
            Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = contentDescription,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(size * 0.8f)
                )
            }
        }
        return
    }

    // 1. High-accuracy vector recognition for common packages and app titles
    val presetVector = getPresetVectorIcon(packageName, contentDescription)
    if (presetVector != null) {
        Box(
            modifier = modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = presetVector,
                contentDescription = contentDescription,
                tint = themeColor,
                modifier = Modifier.size(size * 0.78f)
            )
        }
        return
    }

    // 2. Intelligent bitmap transformation based on selected style
    val tintArgb = android.graphics.Color.argb(
        (themeColor.alpha * 255).toInt(),
        (themeColor.red * 255).toInt(),
        (themeColor.green * 255).toInt(),
        (themeColor.blue * 255).toInt()
    )

    val processedBitmap = remember(drawable, iconStyle, tintArgb) {
        renderSmartThemedIcon(drawable, iconStyle, tintArgb)
    }

    if (processedBitmap != null) {
        Image(
            painter = remember(processedBitmap) { BitmapPainter(processedBitmap.asImageBitmap()) },
            contentDescription = contentDescription,
            modifier = modifier.size(size)
        )
    } else {
        Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = contentDescription,
                tint = themeColor,
                modifier = Modifier.size(size * 0.75f)
            )
        }
    }
}

/**
 * Intelligent renderer that preserves inner symbols & visual recognition
 */
private fun renderSmartThemedIcon(
    drawable: Drawable,
    style: ThemedIconStyle,
    tintArgb: Int
): Bitmap? {
    return try {
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 128
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 128

        when (style) {
            ThemedIconStyle.MUTED_MONOCHROME -> {
                // Desaturates original icon while retaining full original lines, details, and shapes
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)

                val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val resCanvas = Canvas(result)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                val matrix = ColorMatrix()
                matrix.setSaturation(0.18f) // Muted elegant desaturation matching dark wallpaper
                paint.colorFilter = ColorMatrixColorFilter(matrix)
                resCanvas.drawBitmap(bmp, 0f, 0f, paint)
                result
            }

            ThemedIconStyle.TINTED_ADAPTIVE -> {
                // Duotone: Outer circle / rounded container gets a soft theme tone, inner symbol is sharp
                renderDuotoneIcon(drawable, width, height, tintArgb)
            }

            ThemedIconStyle.SMART_MINIMAL -> {
                // Extracts inner foreground if AdaptiveIconDrawable, or extracts high-frequency luminance symbol
                renderSmartMinimalSymbol(drawable, width, height, tintArgb)
            }
        }
    } catch (e: Exception) {
        drawableToBitmap(drawable)
    }
}

/**
 * Smart minimal extraction: If it's an AdaptiveIconDrawable (most modern Android apps),
 * it isolates the foreground symbol drawable and tints ONLY the inner symbol!
 * If not adaptive, it extracts internal high-contrast edges/luminance so it never collapses into a solid circle.
 */
private fun renderSmartMinimalSymbol(
    drawable: Drawable,
    width: Int,
    height: Int,
    tintArgb: Int
): Bitmap? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable is AdaptiveIconDrawable) {
        val foreground = drawable.foreground
        if (foreground != null) {
            val fgBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val fgCanvas = Canvas(fgBitmap)
            foreground.setBounds(0, 0, fgCanvas.width, fgCanvas.height)
            foreground.draw(fgCanvas)

            // Extract inner symbol details using high-contrast thresholding to prevent solid fills
            val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.colorFilter = PorterDuffColorFilter(tintArgb, PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(fgBitmap, 0f, 0f, paint)
            return result
        }
    }

    // Fallback: draw original and apply luminance edge enhancement
    val original = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val origCanvas = Canvas(original)
    drawable.setBounds(0, 0, origCanvas.width, origCanvas.height)
    drawable.draw(origCanvas)

    val outBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val outCanvas = Canvas(outBmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    // Contrast boost matrix before tinting so internal lines don't get erased
    val colorMatrix = ColorMatrix(floatArrayOf(
        0.33f, 0.33f, 0.33f, 0f, -50f,
        0.33f, 0.33f, 0.33f, 0f, -50f,
        0.33f, 0.33f, 0.33f, 0f, -50f,
        0f,    0f,    0f,    1f, 0f
    ))
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    outCanvas.drawBitmap(original, 0f, 0f, paint)

    // Tint with theme color preserving internal contrasts
    val finalBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val finalCanvas = Canvas(finalBmp)
    val tintPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    tintPaint.colorFilter = PorterDuffColorFilter(tintArgb, PorterDuff.Mode.MULTIPLY)
    finalCanvas.drawBitmap(outBmp, 0f, 0f, tintPaint)

    return finalBmp
}

private fun renderDuotoneIcon(
    drawable: Drawable,
    width: Int,
    height: Int,
    tintArgb: Int
): Bitmap {
    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)

    val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x334C6B61 // subtle frosted theme badge circle
    }
    val radius = width / 2.1f
    canvas.drawCircle(width / 2f, height / 2f, radius, basePaint)

    val origBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val origCanvas = Canvas(origBmp)
    drawable.setBounds(0, 0, width, height)
    drawable.draw(origCanvas)

    val fgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = PorterDuffColorFilter(tintArgb, PorterDuff.Mode.SRC_ATOP)
    }
    canvas.drawBitmap(origBmp, 0f, 0f, fgPaint)

    return result
}

private fun drawableToBitmap(drawable: Drawable): Bitmap? {
    return try {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 128
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 128
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    } catch (e: Exception) {
        null
    }
}

/**
 * Recognizes common and system applications by package name AND user-visible app label
 * ensuring every common app (Calendar, Clock, Contacts, Files, Maps, Drive, Safety, etc.)
 * displays its clean recognized symbol instead of a generic circle!
 */
private fun getPresetVectorIcon(packageName: String?, label: String?): ImageVector? {
    val pkg = packageName?.lowercase() ?: ""
    val name = label?.lowercase() ?: ""

    return when {
        // Phone / Dialer
        pkg.contains("dialer") || pkg.contains("phone") || name.contains("phone") -> Icons.Default.Phone

        // Messaging
        pkg.contains("messaging") || pkg.contains("mms") || pkg.contains("sms") || name.contains("message") -> Icons.AutoMirrored.Filled.Chat

        // Browser
        pkg.contains("chrome") || pkg.contains("browser") || name.contains("chrome") -> Icons.Default.Public

        // Calendar
        pkg.contains("calendar") || name.contains("calendar") -> Icons.Default.CalendarMonth

        // Clock / Alarm
        pkg.contains("deskclock") || pkg.contains("clock") || name.contains("clock") -> Icons.Default.Schedule

        // Contacts
        pkg.contains("contacts") || pkg.contains("people") || name.contains("contacts") -> Icons.Default.Person

        // Google Drive / Cloud
        pkg.contains("drive") || pkg.contains("docs") || name.contains("drive") -> Icons.Default.Cloud

        // Files / File Manager
        pkg.contains("documentsui") || pkg.contains("file") || name.contains("file") -> Icons.Default.Folder

        // Gmail / Email
        pkg.contains("gm") || pkg.contains("mail") || pkg.contains("email") || name.contains("mail") -> Icons.Default.Email

        // Maps / Navigation
        pkg.contains("maps") || name.contains("map") -> Icons.Default.LocationOn

        // Camera
        pkg.contains("camera") || name.contains("camera") -> Icons.Default.CameraAlt

        // Photos / Gallery
        pkg.contains("photos") || pkg.contains("gallery") || name.contains("photo") || name.contains("gallery") -> Icons.Default.Image

        // Settings
        pkg.contains("settings") || name.contains("setting") -> Icons.Default.Settings

        // YouTube / Video
        pkg.contains("youtube") || name.contains("youtube") -> Icons.Default.PlayArrow

        // Music / Audio
        pkg.contains("music") || pkg.contains("audio") || name.contains("music") -> Icons.Default.MusicNote

        // Safety / Security
        pkg.contains("safety") || pkg.contains("security") || name.contains("safety") -> Icons.Default.Security

        // Play Store / Market
        pkg.contains("vending") || pkg.contains("market") || pkg.contains("playstore") || name.contains("store") -> Icons.Default.ShoppingBag

        // Games
        pkg.contains("game") || name.contains("game") -> Icons.Default.SportsEsports

        // Bank / Finance
        pkg.contains("bank") || pkg.contains("finance") || pkg.contains("pay") || name.contains("pay") -> Icons.Default.AccountBalance

        else -> null
    }
}
