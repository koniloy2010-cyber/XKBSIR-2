package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val XkbColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CyberBlack,
    primaryContainer = SurfaceCardGlow,
    onPrimaryContainer = NeonCyan,
    secondary = NeonElectricBlue,
    onSecondary = CyberBlack,
    secondaryContainer = SurfaceCard,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonPowerRed,
    onTertiary = CyberBlack,
    background = CyberBlack,
    onBackground = TextPrimary,
    surface = AmoledDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    error = NeonPowerRed
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = XkbColorScheme,
        typography = Typography,
        content = content
    )
}
