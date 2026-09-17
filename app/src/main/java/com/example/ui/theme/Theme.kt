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
    primary = ElectricYellow,
    onPrimary = Color(0xFF221A00),
    primaryContainer = ElectricYellowDark,
    onPrimaryContainer = Color(0xFFFFF9C4),
    secondary = ElectricBlue,
    onSecondary = Color(0xFF002733),
    secondaryContainer = Color(0xFF004960),
    onSecondaryContainer = ElectricBlueLight,
    background = DarkBg,
    onBackground = Color(0xFFECEFF4),
    surface = DarkSurface,
    onSurface = Color(0xFFECEFF4),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBAC5D6),
    outline = DarkOutline,
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricYellowDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECB3),
    onPrimaryContainer = Color(0xFF3E2723),
    secondary = ElectricBlueDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF006064),
    background = LightBg,
    onBackground = Color(0xFF101726),
    surface = LightSurface,
    onSurface = Color(0xFF101726),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF435269),
    outline = LightOutline,
)

@Composable
fun CurrentDetectorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded electric colors
    content: @Composable () -> Unit,
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
