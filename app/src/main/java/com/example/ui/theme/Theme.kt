package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccentLight,
    onPrimary = Color(0xFF00363F),
    primaryContainer = CyanAccentMuted,
    onPrimaryContainer = Color(0xFFCEFAFE),

    secondary = PurpleRea,
    onSecondary = Color(0xFF2E1065),
    secondaryContainer = PurpleReaMuted,
    onSecondaryContainer = Color(0xFFEDE9FE),

    tertiary = EmeraldSuccess,
    onTertiary = Color(0xFF022C22),
    tertiaryContainer = EmeraldSuccessMuted,
    onTertiaryContainer = Color(0xFFD1FAE5),

    background = DarkBackground,
    onBackground = TextPrimary,

    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,

    outline = DarkSurfaceBorder,
    outlineVariant = Color(0xFF1E293B),

    error = RoseError,
    onError = Color(0xFF4C0519),
    errorContainer = RoseErrorMuted,
    onErrorContainer = Color(0xFFFFE4E6)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark developer workstation theme by default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
