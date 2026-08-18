package ru.mesh.expressive.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = MdPrimaryLight,
    onPrimary = MdOnPrimaryLight,
    primaryContainer = MdPrimaryContainerLight,
    onPrimaryContainer = MdOnPrimaryContainerLight,
    secondary = MdSecondaryLight,
    onSecondary = MdOnSecondaryLight,
    secondaryContainer = MdSecondaryContainerLight,
    onSecondaryContainer = MdOnSecondaryContainerLight,
    tertiary = MdTertiaryLight,
    onTertiary = MdOnTertiaryLight,
    tertiaryContainer = MdTertiaryContainerLight,
    onTertiaryContainer = MdOnTertiaryContainerLight,
    surface = MdSurfaceLight,
    onSurface = MdOnSurfaceLight,
    surfaceVariant = MdSurfaceVariantLight,
    onSurfaceVariant = MdOnSurfaceVariantLight,
    surfaceContainer = MdSurfaceContainerLight,
    surfaceContainerHigh = MdSurfaceContainerHighLight,
    surfaceContainerHighest = MdSurfaceContainerHighestLight,
    surfaceContainerLow = MdSurfaceContainerLowLight,
    surfaceContainerLowest = MdSurfaceContainerLowestLight
)

private val DarkColorScheme = darkColorScheme(
    primary = MdPrimaryDark,
    onPrimary = MdOnPrimaryDark,
    primaryContainer = MdPrimaryContainerDark,
    onPrimaryContainer = MdOnPrimaryContainerDark,
    secondary = MdSecondaryDark,
    onSecondary = MdOnSecondaryDark,
    secondaryContainer = MdSecondaryContainerDark,
    onSecondaryContainer = MdOnSecondaryContainerDark,
    tertiary = MdTertiaryDark,
    onTertiary = MdOnTertiaryDark,
    tertiaryContainer = MdTertiaryContainerDark,
    onTertiaryContainer = MdOnTertiaryContainerDark,
    surface = MdSurfaceDark,
    onSurface = MdOnSurfaceDark,
    surfaceVariant = MdSurfaceVariantDark,
    onSurfaceVariant = MdOnSurfaceVariantDark,
    surfaceContainer = MdSurfaceContainerDark,
    surfaceContainerHigh = MdSurfaceContainerHighDark,
    surfaceContainerHighest = MdSurfaceContainerHighestDark,
    surfaceContainerLow = MdSurfaceContainerLowDark,
    surfaceContainerLowest = MdSurfaceContainerLowestDark
)

@Composable
fun MeshExpressiveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Monet Dynamic Color enabled by default on Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
