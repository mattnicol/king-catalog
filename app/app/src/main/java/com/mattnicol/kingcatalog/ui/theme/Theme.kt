package com.mattnicol.kingcatalog.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Blood,
    onPrimary = Color.White,
    secondary = Ash,
    onSecondary = Color.White,
    background = Fog,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
)

private val DarkColors = darkColorScheme(
    primary = DarkRed,
    onPrimary = Color.White,
    secondary = Ash,
    onSecondary = Color.White,
    background = Ink,
    onBackground = Fog,
    surface = Color(0xFF2C2C2E),
    onSurface = Fog,
)

@Composable
fun KingCatalogTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
