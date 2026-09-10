package com.smarttoolfactory.tutorial1_1basics.chapter6_graphics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.MeshGradientPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.TutorialChoices
import com.smarttoolfactory.tutorial1_1basics.ui.components.TutorialPage
import com.smarttoolfactory.tutorial1_1basics.ui.components.TutorialSlider
import com.smarttoolfactory.tutorial1_1basics.ui.components.TutorialText2
import com.smarttoolfactory.tutorial1_1basics.ui.components.TutorialToggle
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Preview
@Composable
fun Tutorial6_44Screen() {
    var generation by rememberSaveable { mutableIntStateOf(0) }
    TutorialPage(
        R.string.tutorial6_44_title,
        "Animate four interior vertices in a **3 × 3 patch mesh** to make flowing color fields.",
        "Pause to inspect the points, change the movement amount, or switch palettes. " +
            "The twelve boundary vertices stay fixed. Reset pauses the mesh at its initial positions."
    ) {
        key(generation) {
            AnimatedMeshGradientSample { generation++ }
        }
    }
}

@Composable
private fun AnimatedMeshGradientSample(onReset: () -> Unit) {
    var palette by rememberSaveable { mutableIntStateOf(0) }
    var amplitude by rememberSaveable { mutableFloatStateOf(8f) }
    var showPoints by rememberSaveable { mutableStateOf(true) }
    var running by rememberSaveable { mutableStateOf(false) }
    val phase = remember { Animatable(0f) }

    LaunchedEffect(running) {
        // Cancelling animateTo on pause retains its value; resume finishes that same cycle.
        while (running && isActive) {
            // A disabled system animation scale must not create an immediate repeat loop.
            if (coroutineContext[MotionDurationScale]?.scaleFactor == 0f) break
            phase.animateTo(
                1f,
                tween(((1f - phase.value) * 8_000).roundToInt().coerceAtLeast(1), easing = LinearEasing)
            )
            phase.snapTo(0f)
            yield()
        }
    }

    val painter = remember {
        MeshGradientPainter(rows = 3, columns = 3, hasBicubicColor = true) {
            val colors = MeshPalettes[palette]
            val progress = phase.value
            val movement = amplitude / 100f
            for (row in 0..3) {
                for (column in 0..3) {
                    setVertex(row, column, animatedMeshPosition(row, column, progress, movement),
                        colors[(row * 3 + column * 2) % colors.size])
                }
            }
        }
    }

    MeshGradientPreview(painter, "Animated mesh gradient, ${MeshPaletteNames[palette]}") {
        if (showPoints) {
            for (row in 0..3) {
                for (column in 0..3) {
                    drawMeshPoint(animatedMeshPosition(row, column, phase.value, amplitude / 100f))
                }
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { running = !running }, modifier = Modifier.testTag("mesh-play-pause")) {
            Text(if (running) "Pause" else "Play")
        }
        TextButton(onClick = onReset, modifier = Modifier.testTag("reset-mesh")) { Text("Reset") }
    }
    TutorialChoices(MeshPaletteNames, palette) { palette = it }
    TutorialSlider("Movement (%)", amplitude, 0f..12f) { amplitude = it }
    TutorialToggle("Show vertices", showPoints) { showPoints = it }
    TutorialText2(
        "Keep the painter in remember and read animated state in its configuration lambda. " +
            "MeshGradientPainter observes those reads during drawing, so each frame does not " +
            "need to recompose the controls."
    )
    TutorialText2(
        "Sine and cosine make an eight-second loop with matching endpoints. Each interior point " +
            "has its own phase. Movement is capped at 12% to preserve vertex ordering. " +
            "System animation duration settings are respected."
    )
}

private fun animatedMeshPosition(row: Int, column: Int, phase: Float, amplitude: Float): Offset {
    val base = Offset(column / 3f, row / 3f)
    if (row == 0 || row == 3 || column == 0 || column == 3) return base
    val angle = (2.0 * PI * phase + row * 1.7 + column * 2.3).toFloat()
    return base + Offset(sin(angle) * amplitude, cos(angle) * amplitude)
}
