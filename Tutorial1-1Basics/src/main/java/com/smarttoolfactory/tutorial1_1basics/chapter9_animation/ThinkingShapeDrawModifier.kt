package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import android.graphics.Matrix
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.material3.MaterialTheme
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import kotlin.math.min

@Composable
internal fun rememberThinkingShapeGradientColors(): List<Color> {
    val accentColor = MaterialTheme.colorScheme.primary
    val aquaColor = lerp(accentColor, Color(0xFF38BDF8), 0.45f)
    val mintColor = lerp(accentColor, Color(0xFF2DD4BF), 0.35f)
    val pinkColor = lerp(accentColor, Color(0xFFF472B6), 0.28f)
    return remember(aquaColor, mintColor, pinkColor) {
        listOf(aquaColor, mintColor, pinkColor, aquaColor)
    }
}

internal fun Modifier.thinkingShapeDraw(
    startPolygon: RoundedPolygon,
    endPolygon: RoundedPolygon?,
    morphProgress: Float,
    rotationDegrees: Float,
    visualScale: Float,
    brush: Brush?,
    brushProgress: Float,
    gradientColors: List<Color>,
    showOutline: Boolean,
    outlineColor: Color
): Modifier = drawWithCache {
    val morph = endPolygon
        ?.takeIf { it != startPolygon }
        ?.let { Morph(startPolygon, it) }
    val androidPath = morph?.toPath(progress = morphProgress.coerceIn(0f, 1f))
        ?: startPolygon.toPath()
    val bounds = RectF().also { rect ->
        androidPath.computeBounds(rect, true)
    }
    val targetSize = min(size.width, size.height) * 0.8f * visualScale
    val scaleFactor = if (bounds.width() == 0f || bounds.height() == 0f) {
        1f
    } else {
        min(targetSize / bounds.width(), targetSize / bounds.height())
    }

    val matrix = Matrix().apply {
        postScale(scaleFactor, scaleFactor, bounds.centerX(), bounds.centerY())
        postRotate(rotationDegrees, bounds.centerX(), bounds.centerY())
        postTranslate(
            size.width / 2f - bounds.centerX(),
            size.height / 2f - bounds.centerY()
        )
    }
    androidPath.transform(matrix)

    val composePath = androidPath.asComposePath()
    val appliedBrush = brush ?: thinkingShapeAnimatedBrush(
        colors = gradientColors,
        progress = brushProgress,
        drawSize = size
    )
    val outlineStroke = Stroke(width = size.minDimension * 0.05f)

    onDrawBehind {
        drawPath(
            path = composePath,
            brush = appliedBrush
        )
        if (showOutline) {
            drawPath(
                path = composePath,
                color = outlineColor,
                style = outlineStroke
            )
        }
    }
}

private fun thinkingShapeAnimatedBrush(
    colors: List<Color>,
    progress: Float,
    drawSize: Size
): Brush {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val travelX = (-drawSize.width) + ((drawSize.width * 2f) * clampedProgress)
    return Brush.linearGradient(
        colors = colors,
        start = Offset(travelX, drawSize.height * -0.2f),
        end = Offset(travelX + drawSize.width, drawSize.height * 0.85f)
    )
}
