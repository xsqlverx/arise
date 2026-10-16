package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EvolveColorScheme = darkColorScheme(
    primary = EvolveAccent,
    onPrimary = EvolveBlack,
    primaryContainer = EvolveViolet.copy(alpha = 0.2f),
    onPrimaryContainer = EvolveAccent,
    secondary = EvolveViolet,
    onSecondary = EvolveText,
    secondaryContainer = EvolveRaised,
    onSecondaryContainer = EvolveText,
    tertiary = EvolveWarm,
    onTertiary = EvolveBlack,
    background = EvolveBlack,
    onBackground = EvolveText,
    surface = EvolveSurface,
    onSurface = EvolveText,
    surfaceVariant = EvolveRaised,
    onSurfaceVariant = EvolveMuted,
    outline = EvolveLine,
    outlineVariant = EvolveLine
)

@Composable
fun EvolveTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = EvolveBlack.toArgb()
                window.navigationBarColor = EvolveBlack.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = EvolveColorScheme,
        typography = EvolveTypography,
        content = content
    )
}
