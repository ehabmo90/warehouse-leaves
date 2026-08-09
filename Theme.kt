package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = TextPrimary,
    primaryContainer = SurfaceCard2,
    onPrimaryContainer = TextPrimary,
    secondary = PrimaryPurpleLight,
    onSecondary = TextPrimary,
    secondaryContainer = SurfaceCard3,
    onSecondaryContainer = TextSecondary,
    tertiary = AccentGreen,
    onTertiary = DarkBg,
    tertiaryContainer = AccentGreenContainer,
    onTertiaryContainer = AccentGreen,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = SurfaceCard1,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard2,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderMedium,
    error = DangerRed,
    onError = TextPrimary,
    errorContainer = DangerRedContainer,
    onErrorContainer = DangerRed
)

@Composable
fun LeavesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBg.toArgb()
            window.navigationBarColor = DarkBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
