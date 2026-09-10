package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import com.smarttoolfactory.tutorial1_1basics.ui.components.*

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import kotlin.math.floor
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialPage

@Composable
internal fun Tutorial9_20Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_20_title,
        fitDemoContent = true,
        introduction = "**Morphing Thinking Indicator** — Run the repeating shape sequence and inspect its coordinated rotation, pulse and gradient.",
        examples = listOf(
            AnimationExample("ThinkingShapeDemo", "Morphing Thinking Indicator",
                "Run the repeating shape sequence and inspect its coordinated rotation, pulse and gradient.") { ThinkingShapeDemo() }
        )
    )
}

@Composable
private fun ThinkingShapeDemo() {
    var running by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TutorialToggle("Run thinking indicator", running) { running = it }
        if (running) ThinkingMorphingShape(Modifier.size(120.dp))
        else ThinkingShapeCanvas(buildThinkingMorphPolygon(ThinkingShapeSpec(ThinkingShapeFamily.Ai, 4)),
            modifier = Modifier.size(120.dp), brushProgress = 0f)
    }
}


private val ThinkingShapeSequence = listOf(
    ThinkingShapeSpec(ThinkingShapeFamily.Ai, 4),
    ThinkingShapeSpec(ThinkingShapeFamily.Polygon, 8),
    ThinkingShapeSpec(ThinkingShapeFamily.Star, 5),
    ThinkingShapeSpec(ThinkingShapeFamily.Polygon, 4),
    ThinkingShapeSpec(ThinkingShapeFamily.Star, 8),
    ThinkingShapeSpec(ThinkingShapeFamily.Polygon, 8, ThinkingShapeProfile.Soft),
    ThinkingShapeSpec(ThinkingShapeFamily.Ai, 4),
)
private val ThinkingIndicatorSize = 22.dp
private val ThinkingMorphDurationMillis = ThinkingBrushDurationMillis * ThinkingShapeSequence.lastIndex
private val ThinkingRotationDurationMillis = ThinkingMorphDurationMillis * 2

@Composable
internal fun ThinkingMorphingShape(
    modifier: Modifier = Modifier,
    brushProgress: Float? = null,
    gradientColors: List<Color>? = null,
    outlineColor: Color? = null
) {
    val sharedBrushProgress = brushProgress ?: rememberThinkingBrushProgress(
        label = "thinking_indicator_brush"
    )
    val polygons = remember {
        ThinkingShapeSequence
            .distinct()
            .associateWith(::buildThinkingMorphPolygon)
    }
    val transition = rememberInfiniteTransition(label = "thinking_indicator")
    val morphProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = (ThinkingShapeSequence.lastIndex).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = ThinkingMorphDurationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "thinking_morph_progress"
    )
    val pulseScale by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = ThinkingPulseDurationMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thinking_pulse_scale"
    )
    val rotationDegrees by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = ThinkingRotationDurationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "thinking_rotation"
    )
    val segmentIndex = floor(morphProgress).toInt()
        .coerceIn(0, ThinkingShapeSequence.lastIndex - 1)
    val segmentProgress = distinctThinkingMorphProgress(
        rawProgress = (morphProgress - segmentIndex).coerceIn(0f, 1f)
    )
    val startShape = ThinkingShapeSequence[segmentIndex]
    val endShape = ThinkingShapeSequence[segmentIndex + 1]

    ThinkingShapeCanvas(
        modifier = modifier.size(ThinkingIndicatorSize),
        startPolygon = requireNotNull(polygons[startShape]),
        endPolygon = requireNotNull(polygons[endShape]),
        morphProgress = segmentProgress,
        rotationDegrees = rotationDegrees,
        visualScale = pulseScale,
        brushProgress = sharedBrushProgress,
        gradientColors = gradientColors,
        outlineColor = outlineColor
    )
}

@Composable
internal fun ThinkingShapeCanvas(
    startPolygon: RoundedPolygon,
    modifier: Modifier = Modifier,
    endPolygon: RoundedPolygon? = null,
    morphProgress: Float = 0f,
    rotationDegrees: Float = DefaultThinkingRotationDegrees,
    visualScale: Float = 1f,
    brush: Brush? = null,
    brushProgress: Float? = null,
    gradientColors: List<Color>? = null,
    showOutline: Boolean = true,
    outlineColor: Color? = null
) {
    val chatColors = MaterialTheme.colorScheme
    val animatedBrushProgress = brushProgress ?: rememberThinkingBrushProgress(
        label = "thinking_shape_brush"
    )
    val shapeGradientColors = gradientColors ?: rememberThinkingShapeGradientColors()

    Box(
        modifier = modifier.thinkingShapeDraw(
            startPolygon = startPolygon,
            endPolygon = endPolygon,
            morphProgress = morphProgress,
            rotationDegrees = rotationDegrees,
            visualScale = visualScale,
            brush = brush,
            brushProgress = animatedBrushProgress,
            gradientColors = shapeGradientColors,
            showOutline = showOutline,
            outlineColor = outlineColor ?: chatColors.primary.copy(alpha = 0.32f)
        )
    )
}
