package com.smarttoolfactory.tutorial1_1basics.chapter6_graphics

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.MeshGradientPainter
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import kotlin.math.ceil

internal val MeshPaletteNames = listOf("Sunset", "Lagoon", "Orchid")

// Row-major colors for a 2 x 2 patch mesh (3 x 3 vertices).
internal val MeshPalettes = listOf(
    listOf(
        Color(0xFFFFB86B), Color(0xFFFF6B6B), Color(0xFF843AC9),
        Color(0xFFFF477E), Color(0xFFFFE6A7), Color(0xFFDA469C),
        Color(0xFF5228A8), Color(0xFFB138AD), Color(0xFF262C85)
    ),
    listOf(
        Color(0xFF063D63), Color(0xFF087F8C), Color(0xFF45DFB1),
        Color(0xFF1789B5), Color(0xFFDAF7A6), Color(0xFF2FC6BA),
        Color(0xFF14378A), Color(0xFF377BE6), Color(0xFF79EDD2)
    ),
    listOf(
        Color(0xFF25116B), Color(0xFF7953D2), Color(0xFFE8B7F0),
        Color(0xFF685BDB), Color(0xFFFBD0E8), Color(0xFFDE56A3),
        Color(0xFF95A5F6), Color(0xFF9240B1), Color(0xFF441A72)
    )
)

/** Shared presentation only; each numbered sample defines its own mesh. */
@Composable
internal fun MeshGradientPreview(
    painter: MeshGradientPainter,
    description: String,
    modifier: Modifier = Modifier,
    previewHeight: Dp? = null,
    overlay: DrawScope.() -> Unit = {}
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(previewHeight ?: (maxWidth * 0.75f).coerceAtMost(320.dp))
                .clipToBounds()
                .meshGradient(painter)
                .drawWithCache { onDrawWithContent { drawContent(); overlay() } }
                .semantics { contentDescription = description }
                .testTag("mesh-preview")
        )
    }
}

/**
 * MeshGradientPainter uses drawVertices, hardware accelerated on Android 10+.
 * Earlier Android versions render into a reusable software bitmap of the same size.
 * State is read inside the draw callback on both paths, so animation invalidates drawing.
 */
private fun Modifier.meshGradient(painter: MeshGradientPainter): Modifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        paint(painter, sizeToIntrinsics = false)
    } else {
        drawWithCache {
            val bitmap = ImageBitmap(
                ceil(size.width).toInt().coerceAtLeast(1),
                ceil(size.height).toInt().coerceAtLeast(1)
            )
            val canvas = Canvas(bitmap)
            val scope = CanvasDrawScope()
            onDrawBehind {
                scope.draw(this, layoutDirection, canvas, size) {
                    drawRect(Color.Transparent, blendMode = BlendMode.Clear)
                    with(painter) { draw(size) }
                }
                drawImage(bitmap)
            }
        }
    }

internal fun DrawScope.drawMeshPoint(position: Offset, color: Color = Color.White) {
    val center = Offset(position.x * size.width, position.y * size.height)
    drawCircle(Color.Black.copy(alpha = 0.65f), 6.dp.toPx(), center)
    drawCircle(color, 4.dp.toPx(), center)
    drawCircle(Color.White, 6.dp.toPx(), center, style = Stroke(1.dp.toPx()))
}
