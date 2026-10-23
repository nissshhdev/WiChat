package com.nissshh.wichat.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Textured Background rendering the user's custom SVG wave ribbon pattern
 * (<svg viewBox='0 0 1000 120'> with wave lines spaced every 30 units)
 * dynamically adapted for both Light Mode and Dark Mode.
 */
@Composable
fun TexturedBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.1f // Detect dark theme
    val baseBackground = MaterialTheme.colorScheme.background

    // Ultra-subtle ambient watermark styling:
    // In Dark mode: very gentle #222222 at 0.35 opacity so text & icons pop with maximum contrast
    // In Light mode: faint slate at 0.40 opacity
    val waveStrokeColor = if (isDark) {
        Color(0xFF222222).copy(alpha = 0.35f)
    } else {
        Color(0xFFCBD5E1).copy(alpha = 0.40f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBackground)
            .drawBehind {
                val unitWidth = size.width
                val scale = unitWidth / 500f // 500 units per repeat (SVG period)
                val patternHeight = 120f * scale
                val strokeWidthPx = 1.2f * density // Refined, crisp thin stroke so UI elements stay razor sharp

                // Y-offsets from SVG: 75, 45, 105, 15, -15, 135 (every 30 units)
                val baseOffsets = floatArrayOf(75f, 45f, 105f, 15f, -15f, 135f)

                var tileY = -patternHeight
                while (tileY < size.height + patternHeight) {
                    for (baseY in baseOffsets) {
                        val path = Path()
                        val startY = tileY + (baseY * scale)

                        // Start at x = 0
                        path.moveTo(0f, startY)

                        var currentX = 0f
                        var goingDown = true
                        while (currentX < size.width + (250f * scale)) {
                            val nextX = currentX + (250f * scale)
                            val cp1X = currentX + (125f * scale)
                            val cp2X = currentX + (125f * scale)
                            val cpY = if (goingDown) startY + (30f * scale) else startY - (30f * scale)
                            val endY = startY

                            // Cubic Bézier matching 'c 125 30 250 30 250 0' / 'c 125 -30 250 -30 250 0'
                            path.cubicTo(
                                x1 = cp1X,
                                y1 = cpY,
                                x2 = cp2X,
                                y2 = cpY,
                                x3 = nextX,
                                y3 = endY
                            )
                            currentX = nextX
                            goingDown = !goingDown
                        }

                        drawPath(
                            path = path,
                            color = waveStrokeColor,
                            style = Stroke(width = strokeWidthPx)
                        )
                    }
                    tileY += patternHeight
                }
            },
        content = content
    )
}

