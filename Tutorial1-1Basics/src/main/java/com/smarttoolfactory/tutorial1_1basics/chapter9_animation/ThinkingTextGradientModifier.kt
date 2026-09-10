package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

internal fun Modifier.thinkingTextGradient(
    brushProgress: Float,
    baseColor: Color,
    highlightColor: Color
): Modifier = graphicsLayer {
    compositingStrategy = CompositingStrategy.Offscreen
}.drawWithCache {
    val brush = thinkingTextAnimatedBrush(
        colors = listOf(baseColor, highlightColor, baseColor),
        progress = brushProgress,
        drawSize = size
    )
    onDrawWithContent {
        drawContent()
        drawRect(
            brush = brush,
            blendMode = BlendMode.SrcAtop
        )
    }
}

private fun thinkingTextAnimatedBrush(
    colors: List<Color>,
    progress: Float,
    drawSize: Size
): Brush {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val travelX = (-drawSize.width) + ((drawSize.width * 2f) * clampedProgress)
    return Brush.horizontalGradient(
        colors = colors,
        startX = travelX,
        endX = travelX + drawSize.width
    )
}
