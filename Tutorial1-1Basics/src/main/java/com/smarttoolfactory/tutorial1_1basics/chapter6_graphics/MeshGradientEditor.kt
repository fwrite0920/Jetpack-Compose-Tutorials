package com.smarttoolfactory.tutorial1_1basics.chapter6_graphics

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.MeshGradientPainter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.tutorial1_1basics.ui.Material3TutorialTheme
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt

/** The static editor and keyframe editor share topology, selection, color and gesture controls. */
@Composable
internal fun MeshGradientEditorPage(@StringRes title: Int, animated: Boolean) {
    val state = rememberSaveable(saver = MeshGradientEditorState.Saver) { MeshGradientEditorState() }
    var bicubic by rememberSaveable { mutableStateOf(true) }
    var showPoints by rememberSaveable { mutableStateOf(true) }
    var duration by rememberSaveable { mutableFloatStateOf(8f) }
    var previewing by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    var vertexMenu by remember { mutableStateOf(false) }
    val phase = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(playing, duration) {
        while (playing && isActive) {
            if (coroutineContext[MotionDurationScale]?.scaleFactor == 0f) break
            phase.animateTo(1f, tween(
                ((1f - phase.value) * duration * 1_000).roundToInt().coerceAtLeast(1),
                easing = LinearEasing
            ))
            phase.snapTo(0f)
            yield()
        }
    }

    // A retired painter can draw once more before its modifier is updated. Give each topology
    // its own state holder so that painter never receives a different number of vertices.
    // Edits update this holder; animation still reads only in the drawing phase.
    val framesForTopology = key(state.rows, state.columns) { rememberUpdatedState(state.frames) }
    val painter = remember(state.rows, state.columns, bicubic) {
        MeshGradientPainter(state.rows, state.columns, hasBicubicColor = bicubic) {
            val fraction = if (previewing) meshFrameFraction(phase.value) else state.activeFrame.toFloat()
            val start = framesForTopology.value[0]
            val end = framesForTopology.value[1]
            start.indices.forEach { index ->
                setVertex(index / (columns + 1), index % (columns + 1),
                    start[index].position + (end[index].position - start[index].position) * fraction,
                    lerp(start[index].color, end[index].color, fraction))
            }
        }
    }

    Material3TutorialTheme {
        Surface(Modifier.fillMaxSize()) {
            BoxWithConstraints(Modifier.fillMaxSize().padding(12.dp)) {
                val wide = maxWidth >= 600.dp
                val previewHeight = if (wide) (maxHeight * 0.6f).coerceIn(120.dp, 320.dp)
                    else (maxHeight * 0.31f).coerceIn(100.dp, 230.dp)

                val preview: @Composable () -> Unit = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(title), style = MaterialTheme.typography.titleLarge)
                        Text(if (previewing) "Animation preview · choose a frame below to edit."
                            else "Tap a vertex to select it, then drag it or use the controls.",
                            style = MaterialTheme.typography.bodySmall)
                        MeshGradientPreview(
                            painter = painter,
                            description = "Editable mesh with ${(state.rows + 1) * (state.columns + 1)} vertices",
                            modifier = Modifier
                                .pointerInput(state, previewing, showPoints) {
                                    if (!previewing && showPoints) detectTapGestures { point ->
                                        state.selectedIndex = nearestMeshVertex(state, point, size.width, size.height)
                                    }
                                }
                                .pointerInput(state, previewing, showPoints) {
                                    var dragging = false
                                    if (!previewing && showPoints) detectDragGestures(
                                        onDragStart = { point ->
                                            val index = nearestMeshVertex(state, point, size.width, size.height)
                                            val vertex = state.frames[state.activeFrame][index].position
                                            val distance = (point - Offset(vertex.x * size.width, vertex.y * size.height)).getDistance()
                                            dragging = distance <= 36.dp.toPx()
                                            if (dragging) state.selectedIndex = index
                                        },
                                        onDragEnd = { dragging = false },
                                        onDragCancel = { dragging = false }
                                    ) { change, delta ->
                                        if (dragging) {
                                            change.consume()
                                            state.moveSelected(state.selectedVertex.position +
                                                Offset(delta.x / size.width, delta.y / size.height))
                                        }
                                    }
                                },
                            previewHeight = previewHeight
                        ) {
                            if (showPoints) {
                                val fraction = if (previewing) meshFrameFraction(phase.value) else state.activeFrame.toFloat()
                                state.frames[0].indices.forEach { index ->
                                    val start = state.frames[0][index]
                                    val end = state.frames[1][index]
                                    val position = start.position + (end.position - start.position) * fraction
                                    drawMeshPoint(position, lerp(start.color, end.color, fraction))
                                    if (index == state.selectedIndex) {
                                        drawCircle(Color.White, 11.dp.toPx(),
                                            Offset(position.x * size.width, position.y * size.height),
                                            style = Stroke(2.dp.toPx()))
                                    }
                                }
                            }
                        }
                        Text("${state.rows + 1} rows × ${state.columns + 1} columns · " +
                            "${(state.rows + 1) * (state.columns + 1)} vertices",
                            style = MaterialTheme.typography.labelLarge, modifier = Modifier.testTag("mesh-grid-size"))
                    }
                }

                val controls: @Composable ColumnScope.() -> Unit = {
                    if (animated) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Start", "End").forEachIndexed { index, label ->
                                FilterChip(selected = !previewing && state.activeFrame == index,
                                    onClick = { playing = false; previewing = false; state.activeFrame = index },
                                    label = { Text("Edit $label") }, modifier = Modifier.testTag("mesh-frame-$index"))
                            }
                            Button(onClick = {
                                if (playing) playing = false
                                else scope.launch {
                                    if (!previewing) phase.snapTo(state.activeFrame * 0.5f)
                                    previewing = true
                                    playing = true
                                }
                            }, modifier = Modifier.testTag("mesh-play-pause")) { Text(if (playing) "Pause" else "Play") }
                        }
                    }
                    if (!previewing) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = state::addRow, enabled = state.rows < MeshGradientEditorState.MaxPatches,
                                modifier = Modifier.testTag("mesh-add-row")) { Text("Add row") }
                            OutlinedButton(onClick = state::addColumn, enabled = state.columns < MeshGradientEditorState.MaxPatches,
                                modifier = Modifier.testTag("mesh-add-column")) { Text("Add column") }
                        }
                        Text("Adds vertices between the selected row/column and its next neighbor " +
                            "(before the border when selected). Existing points and colors stay in place. Limit: 6 × 6 vertices.",
                            style = MaterialTheme.typography.bodySmall)
                        Box {
                            OutlinedButton(onClick = { vertexMenu = true }, modifier = Modifier.testTag("mesh-select-vertex")) {
                                Text("Vertex ${state.selectedRow + 1}, ${state.selectedColumn + 1}")
                            }
                            DropdownMenu(vertexMenu, onDismissRequest = { vertexMenu = false }, modifier = Modifier.heightIn(max = 280.dp)) {
                                state.frames[state.activeFrame].indices.forEach { index ->
                                    DropdownMenuItem(text = { Text("Vertex ${index / (state.columns + 1) + 1}, ${index % (state.columns + 1) + 1}") },
                                        onClick = { state.selectedIndex = index; vertexMenu = false },
                                        modifier = Modifier.testTag("mesh-vertex-$index"))
                                }
                            }
                        }
                        val vertex = state.selectedVertex
                        val xRange = state.positionRange(horizontal = true)
                        val yRange = state.positionRange(horizontal = false)
                        MeshEditorSlider("Vertex X (%)", vertex.position.x * 100f,
                            xRange.start * 100f..xRange.endInclusive * 100f) {
                            state.moveSelected(vertex.position.copy(x = it / 100f))
                        }
                        MeshEditorSlider("Vertex Y (%)", vertex.position.y * 100f,
                            yRange.start * 100f..yRange.endInclusive * 100f) {
                            state.moveSelected(vertex.position.copy(y = it / 100f))
                        }
                        Text("Corners stay fixed. Edge points slide along their edge. Interior points stay between neighbors.",
                            style = MaterialTheme.typography.bodySmall)
                        MeshVertexColorControls(vertex.color, state::setColor)
                    }
                    if (animated) {
                        MeshEditorSlider("Cycle duration (s)", duration, 4f..16f) { duration = it }
                        Text("Edit each frame's positions and colors, then Play to blend Start → End → Start. " +
                            "Rows and columns are inserted into both frames so their vertices match. Pause freezes the preview.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Bicubic colors", Modifier.weight(1f))
                        Switch(bicubic, { bicubic = it }, modifier = Modifier.testTag("Bicubic colors"))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Show vertices", Modifier.weight(1f))
                        Switch(showPoints, { showPoints = it }, modifier = Modifier.testTag("Show vertices"))
                    }
                    TextButton(onClick = {
                        playing = false; previewing = false; vertexMenu = false
                        state.reset(); duration = 8f; bicubic = true; showPoints = true
                    }, modifier = Modifier.testTag("reset-mesh")) { Text("Reset") }
                }

                if (wide) {
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Box(Modifier.weight(1f)) { preview() }
                        Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())
                            .testTag("mesh-editor-controls"), verticalArrangement = Arrangement.spacedBy(10.dp), content = controls)
                    }
                } else {
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        preview()
                        HorizontalDivider()
                        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                            .testTag("mesh-editor-controls"), verticalArrangement = Arrangement.spacedBy(10.dp), content = controls)
                    }
                }
            }
        }
    }
}

@Composable
private fun MeshEditorSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    val enabled = range.start < range.endInclusive
    Text("$label: ${value.roundToInt()}${if (enabled) "" else " (fixed)"}", style = MaterialTheme.typography.bodyMedium)
    Slider(value, onChange, enabled = enabled, valueRange = if (enabled) range else 0f..100f,
        modifier = Modifier.testTag(label).semantics { contentDescription = label })
}

@Composable
private fun MeshVertexColorControls(color: Color, onChange: (Color) -> Unit) {
    Text("Vertex color · #%06X".format(color.toArgb() and 0xFFFFFF), modifier = Modifier.testTag("mesh-vertex-color"))
    val swatches = listOf("Coral" to Color(0xFFFF6B6B), "Gold" to Color(0xFFFFCC66),
        "Mint" to Color(0xFF45DFB1), "Blue" to Color(0xFF377BE6),
        "Violet" to Color(0xFF843AC9), "White" to Color.White)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        swatches.forEach { (label, swatch) ->
            Box(Modifier.size(48.dp).selectable(color == swatch, role = Role.RadioButton, onClick = { onChange(swatch) })
                .background(swatch, CircleShape)
                .border(if (color == swatch) 3.dp else 1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                .semantics { contentDescription = "Color $label" })
        }
    }
    MeshEditorSlider("Red", color.red * 255f, 0f..255f) { onChange(color.copy(red = it / 255f)) }
    MeshEditorSlider("Green", color.green * 255f, 0f..255f) { onChange(color.copy(green = it / 255f)) }
    MeshEditorSlider("Blue", color.blue * 255f, 0f..255f) { onChange(color.copy(blue = it / 255f)) }
}

private fun meshFrameFraction(phase: Float) = ((1.0 - cos(2.0 * PI * phase)) * 0.5).toFloat()

private fun nearestMeshVertex(state: MeshGradientEditorState, point: Offset, width: Int, height: Int): Int =
    state.frames[state.activeFrame].indices.minBy { index ->
        val position = state.frames[state.activeFrame][index].position
        (point - Offset(position.x * width, position.y * height)).getDistanceSquared()
    }
