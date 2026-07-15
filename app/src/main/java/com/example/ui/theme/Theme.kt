package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonLime,
    secondary = NeonCyan,
    tertiary = NeonCrimson,
    background = RealBg,
    surface = RealSurface,
    surfaceVariant = RealSurfaceVariant,
    onPrimary = RealBg,
    onSecondary = RealBg,
    onTertiary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextGray,
    outline = BorderColor
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark mode for a professional night-usage athletic design
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our beautiful custom neon athletic branding
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
