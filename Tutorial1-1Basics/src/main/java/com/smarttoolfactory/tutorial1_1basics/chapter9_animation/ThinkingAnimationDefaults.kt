package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

internal const val ThinkingBrushDurationMillis = 1600
private const val ThinkingTextBrushDurationMillis = 1600
internal const val ThinkingPulseDurationMillis = 900
internal const val ThinkingShapeMinVertices = 3
internal const val ThinkingShapeMaxVertices = 10
internal const val DefaultThinkingRotationDegrees = 0f

private const val ThinkingMorphHoldFraction = 0.18f

@Composable
internal fun rememberThinkingBrushProgress(label: String): Float {
    return rememberThinkingAnimatedProgress(
        label = label,
        durationMillis = ThinkingBrushDurationMillis
    )
}

@Composable
internal fun rememberThinkingTextBrushProgress(label: String): Float {
    return rememberThinkingAnimatedProgress(
        label = label,
        durationMillis = ThinkingTextBrushDurationMillis
    )
}

@Composable
private fun rememberThinkingAnimatedProgress(
    label: String,
    durationMillis: Int
): Float {
    val transition = rememberInfiniteTransition(label = label)
    val brushProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "${label}_progress"
    )
    return brushProgress
}

internal fun distinctThinkingMorphProgress(rawProgress: Float): Float {
    return when {
        rawProgress <= ThinkingMorphHoldFraction -> 0f
        rawProgress >= 1f - ThinkingMorphHoldFraction -> 1f
        else -> FastOutSlowInEasing.transform(
            (rawProgress - ThinkingMorphHoldFraction) /
                (1f - (ThinkingMorphHoldFraction * 2f))
        )
    }
}
