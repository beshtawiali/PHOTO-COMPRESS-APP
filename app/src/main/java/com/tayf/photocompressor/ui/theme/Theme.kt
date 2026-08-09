package com.tayf.photocompressor.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = RoyalPurplePrimary,
    onPrimary = RoyalPurpleOnPrimary,
    primaryContainer = RoyalPurplePrimaryContainer,
    onPrimaryContainer = RoyalPurpleOnPrimaryContainer,
    secondary = RoyalPurpleSecondary,
    onSecondary = RoyalPurpleOnSecondary,
    secondaryContainer = RoyalPurpleSecondaryContainer,
    onSecondaryContainer = RoyalPurpleOnSecondaryContainer,
    background = BackgroundLightLavender,
    onBackground = OnBackgroundDarkText,
    surface = SurfacePureWhite,
    onSurface = OnSurfaceDarkText,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantMuted,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    outline = OutlineBorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = RoyalPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF282A52),
    onPrimaryContainer = RoyalPurplePrimaryContainer,
    secondary = RoyalPurpleSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF222442),
    onSecondaryContainer = RoyalPurpleSecondaryContainer,
    background = BackgroundDarkTheme,
    onBackground = OnBackgroundDarkTheme,
    surface = SurfaceDarkTheme,
    onSurface = OnSurfaceDarkTheme,
    surfaceVariant = Color(0xFF232544),
    onSurfaceVariant = Color(0xFFA8B0CA),
    surfaceContainer = Color(0xFF1C1E3A),
    surfaceContainerHigh = Color(0xFF272A50),
    outline = Color(0xFF3B3E6A)
)

@Composable
fun PhotoCompressorTheme(
    themeMode: String = "system",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
