package com.lzinnbarbosa.komadeck.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val KomaDeckLightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerIndigo,
    onPrimaryContainer = OnPrimaryContainerIndigo,
    secondary = AnimeBadgeContent,
    onSecondary = OnPrimaryWhite,
    secondaryContainer = AnimeBadgeContainer,
    onSecondaryContainer = AnimeBadgeContent,
    tertiary = MangaBadgeContent,
    onTertiary = OnPrimaryWhite,
    tertiaryContainer = MangaBadgeContainer,
    onTertiaryContainer = MangaBadgeContent,
    background = BackgroundOffWhite,
    onBackground = TextPrimaryInk,
    surface = SurfaceWhite,
    onSurface = TextPrimaryInk,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = TextSecondarySlate,
    outline = CardBorderLight,
    outlineVariant = CardBorderLight,
    error = ErrorCrimson,
    onError = OnPrimaryWhite,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorCrimson,
)

private val KomaDeckShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
)

@Composable
fun KomaDeckTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = KomaDeckLightColorScheme,
        typography = KomaDeckTypography,
        shapes = KomaDeckShapes,
        content = content,
    )
}
