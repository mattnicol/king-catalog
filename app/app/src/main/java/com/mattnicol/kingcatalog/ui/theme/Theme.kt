package com.mattnicol.kingcatalog.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val GothicDarkColors = darkColorScheme(
    primary = CrimsonRed,
    onPrimary = ParchmentWhite,
    primaryContainer = DimCrimson,
    onPrimaryContainer = Parchment,
    secondary = Ash,
    onSecondary = ParchmentWhite,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = Parchment,
    tertiary = ParchmentDim,
    onTertiary = NearBlack,
    background = NearBlack,
    onBackground = ParchmentWhite,
    surface = SurfaceDark,
    onSurface = ParchmentWhite,
    surfaceVariant = SurfaceMid,
    onSurfaceVariant = Parchment,
    outline = IvoryMuted,
    outlineVariant = SurfaceElevated,
    error = Color(0xFFCF6679),
    onError = NearBlack,
)

private val GothicLightColors = lightColorScheme(
    primary = BloodRed,
    onPrimary = ParchmentWhite,
    primaryContainer = DimCrimson,
    onPrimaryContainer = ParchmentWhite,
    secondary = Ash,
    onSecondary = ParchmentWhite,
    background = Color(0xFF1A1010),
    onBackground = ParchmentWhite,
    surface = Color(0xFF200F0F),
    onSurface = ParchmentWhite,
    surfaceVariant = SurfaceMid,
    onSurfaceVariant = Parchment,
    outline = IvoryMuted,
)

// Sharp gothic shapes – reduced corner radii throughout
val GothicShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(2.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
)

@Composable
fun KingCatalogTheme(
    darkTheme: Boolean = true,          // Gothic app defaults to dark
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) GothicDarkColors else GothicLightColors,
        typography = Typography,
        shapes = GothicShapes,
        content = content,
    )
}
