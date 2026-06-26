package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = LosLimeAccent,
    onPrimary = LosOliveText,
    primaryContainer = LosDarkOlive,
    onPrimaryContainer = LosLimeAccent,
    secondary = LosTextMuted,
    onSecondary = LosDarkBg,
    background = LosDarkBg,
    onBackground = LosTextOnDark,
    surface = LosSurface,
    onSurface = LosTextOnDark,
    surfaceVariant = LosSurfaceLighter,
    onSurfaceVariant = LosTextOnDark,
    error = LosError,
    onError = LosTextOnDark,
    errorContainer = LosDarkOlive,
    onErrorContainer = LosError
)

// We force the dark theme since the production floor requires a high-contrast eye-safe dark interface
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, 
    dynamicColor: Boolean = false, // disabled to enforce corporate branding
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
