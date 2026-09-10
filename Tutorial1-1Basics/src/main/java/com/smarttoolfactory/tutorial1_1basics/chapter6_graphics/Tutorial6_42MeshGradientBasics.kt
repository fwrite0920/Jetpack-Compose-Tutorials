package com.smarttoolfactory.tutorial1_1basics.chapter6_graphics

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.MeshGradientPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.TutorialChoices
import com.smarttoolfactory.tutorial1_1basics.ui.components.TutorialPage
import com.smarttoolfactory.tutorial1_1basics.ui.components.TutorialText2
import com.smarttoolfactory.tutorial1_1basics.ui.components.TutorialToggle

@Preview
@Composable
fun Tutorial6_42Screen() {
    var palette by rememberSaveable { mutableIntStateOf(0) }
    var showPoints by rememberSaveable { mutableStateOf(true) }

    // Keep the painter. Read palette state in its drawing lambda to update the colors.
    val painter = remember {
        MeshGradientPainter(rows = 1, columns = 1) {
            val colors = MeshPalettes[palette]
            setVertex(0, 0, Offset(0f, 0f), colors[0])
            setVertex(0, 1, Offset(1f, 0f), colors[2])
            setVertex(1, 0, Offset(0f, 1f), colors[6])
            setVertex(1, 1, Offset(1f, 1f), colors[8])
        }
    }

    TutorialPage(
        R.string.tutorial6_42_title,
        "**MeshGradientPainter** gives each vertex a color and blends those colors across a surface.",
        "Start with one patch: rows = 1 and columns = 1 require four vertices. " +
            "Choose a palette and inspect the corner colors."
    ) {
        MeshGradientPreview(painter, "Four-corner mesh gradient, ${MeshPaletteNames[palette]}") {
            if (showPoints) {
                drawMeshPoint(Offset(0f, 0f))
                drawMeshPoint(Offset(1f, 0f))
                drawMeshPoint(Offset(0f, 1f))
                drawMeshPoint(Offset(1f, 1f))
            }
        }
        TutorialChoices(MeshPaletteNames, palette) { palette = it }
        TutorialToggle("Show vertices", showPoints) { showPoints = it }
        TutorialText2(
            "setVertex(row, column, position, color) uses normalized positions: " +
                "(0, 0) is the top-left and (1, 1) is the bottom-right. " +
                "The same mesh therefore fits any drawing size."
        )
        TutorialText2(
            "Rows and columns count patches, not vertices. A mesh needs " +
                "(rows + 1) × (columns + 1) vertices. Configure every vertex; " +
                "an omitted vertex defaults to transparent at (0, 0)."
        )
        TextButton(
            onClick = { palette = 0; showPoints = true },
            modifier = Modifier.testTag("reset-mesh")
        ) { Text("Reset") }
    }
}
