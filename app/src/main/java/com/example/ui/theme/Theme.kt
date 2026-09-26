package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF53DC93),
    onPrimary = Color(0xFF00381E),
    primaryContainer = KMartGreenDark,
    onPrimaryContainer = Color(0xFF86FBB7),
    secondary = Color(0xFFFFB74D),
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF6B3B00),
    onSecondaryContainer = Color(0xFFFFDCC2),
    tertiary = KMartYellowFlash,
    background = KMartDarkBg,
    surface = KMartDarkSurface,
    surfaceVariant = KMartDarkSurfaceVariant,
    onBackground = Color(0xFFE1E8E3),
    onSurface = Color(0xFFE1E8E3),
    onSurfaceVariant = Color(0xFFC0CCC4),
    outline = Color(0xFF43524B)
)

private val LightColorScheme = lightColorScheme(
    primary = KMartGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = KMartGreenContainer,
    onPrimaryContainer = KMartOnGreenContainer,
    secondary = KMartOrangeAccent,
    onSecondary = Color.White,
    secondaryContainer = KMartOrangeLight,
    onSecondaryContainer = Color(0xFF5D2400),
    tertiary = KMartInfo,
    background = KMartLightBg,
    surface = KMartLightSurface,
    surfaceVariant = KMartLightSurfaceVariant,
    onBackground = KMartTextPrimary,
    onSurface = KMartTextPrimary,
    onSurfaceVariant = KMartTextSecondary,
    outline = KMartOutline
)

@Composable
fun JSRKMartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use intentional brand theme for cohesive grocery brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    JSRKMartTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
