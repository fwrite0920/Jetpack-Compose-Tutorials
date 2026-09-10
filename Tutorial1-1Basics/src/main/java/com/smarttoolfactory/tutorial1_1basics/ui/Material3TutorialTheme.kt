package com.smarttoolfactory.tutorial1_1basics.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material.MaterialTheme as Material2Theme
import androidx.compose.material3.MaterialTheme as Material3Theme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

internal enum class TutorialThemeMode { System, Light, Dark }

internal val TutorialLightColors = lightColorScheme(
    primary = Color(0xFF495D92), onPrimary = Color.White,
    primaryContainer = Color(0xFFDAE2FF), onPrimaryContainer = Color(0xFF001A43),
    secondary = Color(0xFF585E71), onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE2F9), onSecondaryContainer = Color(0xFF151B2C),
    tertiary = Color(0xFF735471), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFED7F9), onTertiaryContainer = Color(0xFF2B122B),
    background = Color(0xFFFAF8FF), surface = Color(0xFFFAF8FF),
    onBackground = Color(0xFF1A1B21), onSurface = Color(0xFF1A1B21)
)
internal val TutorialDarkColors = darkColorScheme(
    primary = Color(0xFFB2C5FF), onPrimary = Color(0xFF192E60),
    primaryContainer = Color(0xFF314577), onPrimaryContainer = Color(0xFFDAE2FF),
    secondary = Color(0xFFC0C6DD), onSecondary = Color(0xFF2A3042),
    secondaryContainer = Color(0xFF404659), onSecondaryContainer = Color(0xFFDCE2F9),
    tertiary = Color(0xFFE1BBDD), onTertiary = Color(0xFF422741),
    tertiaryContainer = Color(0xFF5A3D58), onTertiaryContainer = Color(0xFFFED7F9),
    background = Color(0xFF121318), surface = Color(0xFF121318),
    onBackground = Color(0xFFE3E2E9), onSurface = Color(0xFFE3E2E9)
)

/** Scoped to the new tutorials. The application's Material 2 theme stays unchanged. */
@Composable
internal fun Material3TutorialTheme(
    mode: TutorialThemeMode = TutorialThemeMode.System,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val dark = when (mode) {
        TutorialThemeMode.System -> isSystemInDarkTheme()
        TutorialThemeMode.Light -> false
        TutorialThemeMode.Dark -> true
    }
    val context = LocalContext.current
    val scheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (dark) TutorialDarkColors else TutorialLightColors
    // Existing tutorial text and comparison controls read Material 2 locals.
    val colors2 = if (dark) darkColors() else lightColors()
    Material2Theme(colors = colors2.copy(
        primary = scheme.primary, onPrimary = scheme.onPrimary,
        secondary = scheme.secondary, onSecondary = scheme.onSecondary,
        background = scheme.background, onBackground = scheme.onBackground,
        surface = scheme.surface, onSurface = scheme.onSurface,
        error = scheme.error, onError = scheme.onError
    )) {
        CompositionLocalProvider(androidx.compose.material.LocalContentColor provides scheme.onSurface) {
            Material3Theme(colorScheme = scheme, content = content)
        }
    }
}
