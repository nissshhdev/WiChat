package com.nissshh.wichat.core.designsystem.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Super-smooth, Apple-inspired motion design tokens.
 * Fast, responsive, interruptible physics springs and subtle easing.
 */
object MotionTokens {
    // Durations
    const val DurationMicro = 120
    const val DurationStandard = 220
    const val DurationScreen = 280
    const val DurationHero = 340

    // Springs
    fun <T> responsiveSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    fun <T> gentleSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    fun <T> crispTween() = tween<T>(
        durationMillis = DurationStandard
    )
}
