package com.smarttoolfactory.tutorial1_1basics.chapter6_graphics

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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

@Preview
@Composable
fun Tutorial6_43Screen() {
    var palette by rememberSaveable { mutableIntStateOf(0) }
    var centerX by rememberSaveable { mutableFloatStateOf(50f) }
    var centerY by rememberSaveable { mutableFloatStateOf(50f) }
    var bend by rememberSaveable { mutableFloatStateOf(0f) }
    var customTangents by rememberSaveable { mutableStateOf(false) }
    var bicubic by rememberSaveable { mutableStateOf(true) }
    var showPoints by rememberSaveable { mutableStateOf(true) }

    // Interpolation is a constructor option, so changing it creates a new painter.
    // Position, palette and tangent state are read later, while drawing.
    val painter = remember(bicubic) {
        MeshGradientPainter(rows = 2, columns = 2, hasBicubicColor = bicubic) {
            for (row in 0..2) {
                for (column in 0..2) {
                    val color = MeshPalettes[palette][row * 3 + column]
                    if (row == 1 && column == 1) {
                        val curve = bend / 100f
                        setVertex(
                            row, column, Offset(centerX / 100f, centerY / 100f), color,
                            leftControlPoint = if (customTangents) Offset(-1f / 6f, -curve) else Offset.Unspecified,
                            rightControlPoint = if (customTangents) Offset(1f / 6f, curve) else Offset.Unspecified,
                            topControlPoint = if (customTangents) Offset(curve, -1f / 6f) else Offset.Unspecified,
                            bottomControlPoint = if (customTangents) Offset(-curve, 1f / 6f) else Offset.Unspecified
                        )
                    } else {
                        setVertex(row, column, Offset(column / 2f, row / 2f), color)
                    }
                }
            }
        }
    }

    TutorialPage(
        R.string.tutorial6_43_title,
        "A **2 × 2 patch mesh** has nine vertices. Moving its center redistributes color across all four patches.",
        "Move the center with the sliders, then enable custom Bézier tangents and change the bend. " +
            "Compare bilinear and bicubic color interpolation."
    ) {
        MeshGradientPreview(painter, "Nine-vertex mesh gradient, ${MeshPaletteNames[palette]}") {
            if (showPoints) {
                val center = Offset(centerX / 100f, centerY / 100f)
                if (customTangents) {
                    val curve = bend / 100f
                    listOf(Offset(-1f / 6f, -curve), Offset(1f / 6f, curve),
                        Offset(curve, -1f / 6f), Offset(-curve, 1f / 6f)).forEach { tangent ->
                        val handle = center + tangent
                        drawLine(Color.White, Offset(center.x * size.width, center.y * size.height),
                            Offset(handle.x * size.width, handle.y * size.height), 1.dp.toPx())
                        drawMeshPoint(handle, Color.Yellow)
                    }
                }
                for (row in 0..2) {
                    for (column in 0..2) {
                        drawMeshPoint(if (row == 1 && column == 1) center else Offset(column / 2f, row / 2f))
                    }
                }
            }
        }
        TutorialChoices(MeshPaletteNames, palette) { palette = it }
        TutorialSlider("Center X (%)", centerX, 30f..70f) { centerX = it }
        TutorialSlider("Center Y (%)", centerY, 30f..70f) { centerY = it }
        TutorialToggle("Custom Bézier tangents", customTangents) { customTangents = it }
        if (customTangents) {
            TutorialSlider("Tangent bend (%)", bend, -15f..15f) { bend = it }
        }
        TutorialToggle("Bicubic colors", bicubic) { bicubic = it }
        TutorialToggle("Show vertices", showPoints) { showPoints = it }
        TutorialText2(
            "Bézier controls are offsets relative to their vertex. Yellow handles show the center's " +
                "explicit controls. Offset.Unspecified lets Compose infer a smooth tangent; " +
                "Offset.Zero removes that handle's length."
        )
        TutorialText2(
            "hasBicubicColor changes color interpolation, independently of the curved geometry. " +
                "The center stays within 30–70% and the border stays fixed to keep this example bounded."
        )
        TextButton(onClick = {
            palette = 0; centerX = 50f; centerY = 50f; bend = 0f
            customTangents = false; bicubic = true; showPoints = true
        }, modifier = Modifier.testTag("reset-mesh")) { Text("Reset") }
    }
}
