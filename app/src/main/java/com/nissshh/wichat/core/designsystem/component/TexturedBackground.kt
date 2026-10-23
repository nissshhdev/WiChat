package com.nissshh.wichat.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Textured Background providing a refined, tactile mesh & micro-dot texture
 * for both Light Mode and Dark Mode, replacing completely blank solid backgrounds.
 */
@Composable
fun TexturedBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.1f // Detect dark theme
    val baseBackground = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary

    // Subtle radial glow and tactile micro-dots
    val dotColor = if (isDark) {
        Color(0xFF222634).copy(alpha = 0.35f)
    } else {
        Color(0xFFCBD5E1).copy(alpha = 0.40f)
    }

    val glowColor = if (isDark) {
        primaryColor.copy(alpha = 0.07f)
    } else {
        primaryColor.copy(alpha = 0.04f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBackground)
            .drawBehind {
                // 1. Subtle top-right ambient glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor, Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.12f),
                        radius = size.width * 0.75f
                    )
                )

                // 2. Subtle bottom-left ambient glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor.copy(alpha = glowColor.alpha * 0.8f), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.88f),
                        radius = size.width * 0.8f
                    )
                )

                // 3. Tactile micro-grid texture dots (28dp spacing)
                val dotSpacingPx = 28 * density
                val dotRadiusPx = 1.2f * density

                var x = 0f
                while (x < size.width) {
                    var y = 0f
                    while (y < size.height) {
                        drawCircle(
                            color = dotColor,
                            radius = dotRadiusPx,
                            center = Offset(x, y)
                        )
                        y += dotSpacingPx
                    }
                    x += dotSpacingPx
                }
            },
        content = content
    )
}
