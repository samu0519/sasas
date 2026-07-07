package com.dynamicisland.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = AccentBlue,
    background = AmoledBlack,
    surface = SurfaceDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onPrimary = TextPrimary
)

@Composable
fun DynamicIslandTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        content = content
    )
}
