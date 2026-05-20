package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    secondary = NeonGreen,
    tertiary = BrightPink,
    background = TechBlack,
    surface = DeepSlate,
    surfaceVariant = CardSlate,
    onPrimary = TechBlack,
    onSecondary = TechBlack,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark mode by default
    dynamicColor: Boolean = false, // Force custom neon colors
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
