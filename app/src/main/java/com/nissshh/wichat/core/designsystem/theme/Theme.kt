package com.nissshh.wichat.core.designsystem.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LightPrimaryAccent,
    onPrimary = LightSurface,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    background = LightAppBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = LightDivider,
    error = LightError,
    onError = LightSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryAccent,
    onPrimary = DarkAppBackground,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    background = DarkAppBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkDivider,
    error = DarkError,
    onError = DarkSurface
)

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

@Composable
fun WiChatTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val targetScheme = if (isDark) DarkColorScheme else LightColorScheme

    // Smooth subtle crossfade / interpolation on theme switch
    val primary by animateColorAsState(targetScheme.primary, tween(MotionTokens.DurationScreen), label = "primary")
    val background by animateColorAsState(targetScheme.background, tween(MotionTokens.DurationScreen), label = "background")
    val surface by animateColorAsState(targetScheme.surface, tween(MotionTokens.DurationScreen), label = "surface")
    val onSurface by animateColorAsState(targetScheme.onSurface, tween(MotionTokens.DurationScreen), label = "onSurface")
    val outline by animateColorAsState(targetScheme.outline, tween(MotionTokens.DurationScreen), label = "outline")

    val animatedColorScheme = targetScheme.copy(
        primary = primary,
        background = background,
        surface = surface,
        onSurface = onSurface,
        outline = outline
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = background.toArgb()
                window.navigationBarColor = surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = WiChatTypography,
        content = content
    )
}
