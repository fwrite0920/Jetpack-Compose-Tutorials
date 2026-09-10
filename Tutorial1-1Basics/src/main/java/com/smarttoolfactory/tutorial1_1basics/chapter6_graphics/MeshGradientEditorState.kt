package com.smarttoolfactory.tutorial1_1basics.chapter6_graphics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import kotlin.math.min

internal data class EditableMeshVertex(val position: Offset, val color: Color)

/** Rows and columns count patches. Each frame contains (rows + 1) * (columns + 1) vertices. */
internal class MeshGradientEditorState {
    var rows by mutableIntStateOf(2)
        private set
    var columns by mutableIntStateOf(2)
        private set
    var selectedIndex by mutableIntStateOf(4)
    var activeFrame by mutableIntStateOf(0)
    var frames by mutableStateOf(defaultFrames())
        private set

    val selectedRow get() = selectedIndex / (columns + 1)
    val selectedColumn get() = selectedIndex % (columns + 1)
    val selectedVertex get() = frames[activeFrame][selectedIndex]

    fun setColor(color: Color) = updateSelected(selectedVertex.copy(color = color))

    /** Border points stay on their edge; corners stay fixed. Neighbors cannot change order. */
    fun moveSelected(position: Offset) {
        val xRange = positionRange(horizontal = true)
        val yRange = positionRange(horizontal = false)
        updateSelected(selectedVertex.copy(position = Offset(
            position.x.coerceIn(xRange), position.y.coerceIn(yRange)
        )))
    }

    fun positionRange(horizontal: Boolean): ClosedFloatingPointRange<Float> {
        val axisIndex = if (horizontal) selectedColumn else selectedRow
        val axisLast = if (horizontal) columns else rows
        if (axisIndex == 0) return 0f..0f
        if (axisIndex == axisLast) return 1f..1f
        val stride = if (horizontal) 1 else columns + 1
        val vertices = frames[activeFrame]
        val before = vertices[selectedIndex - stride].position
        val after = vertices[selectedIndex + stride].position
        val low = if (horizontal) before.x else before.y
        val high = if (horizontal) after.x else after.y
        val gap = min(0.035f, (high - low) / 4f)
        return (low + gap)..(high - gap)
    }

    private fun updateSelected(vertex: EditableMeshVertex) {
        frames = frames.mapIndexed { index, vertices ->
            if (index == activeFrame) vertices.toMutableList().also { it[selectedIndex] = vertex }
            else vertices
        }
    }

    /** Insert in both frames so every animated vertex keeps the same row/column identity. */
    fun addRow() {
        if (rows >= MaxPatches) return
        val insertion = (selectedRow + 1).coerceAtMost(rows)
        val selectedColumn = selectedColumn
        frames = frames.map { old ->
            buildList {
                for (row in 0..rows + 1) {
                    for (column in 0..columns) {
                        add(if (row == insertion) {
                            midpoint(old[(row - 1) * (columns + 1) + column], old[row * (columns + 1) + column])
                        } else old[(if (row < insertion) row else row - 1) * (columns + 1) + column])
                    }
                }
            }
        }
        rows++
        selectedIndex = insertion * (columns + 1) + selectedColumn
    }

    fun addColumn() {
        if (columns >= MaxPatches) return
        val insertion = (selectedColumn + 1).coerceAtMost(columns)
        val selectedRow = selectedRow
        frames = frames.map { old ->
            buildList {
                for (row in 0..rows) {
                    for (column in 0..columns + 1) {
                        add(if (column == insertion) {
                            midpoint(old[row * (columns + 1) + column - 1], old[row * (columns + 1) + column])
                        } else old[row * (columns + 1) + if (column < insertion) column else column - 1])
                    }
                }
            }
        }
        columns++
        selectedIndex = selectedRow * (columns + 1) + insertion
    }

    fun reset() {
        rows = 2
        columns = 2
        selectedIndex = 4
        activeFrame = 0
        frames = defaultFrames()
    }

    companion object {
        const val MaxPatches = 5

        // Store only Bundle-compatible primitives; colors retain their exact RGB values.
        val Saver = listSaver<MeshGradientEditorState, Any>(
            save = { state ->
                buildList {
                    add(state.rows); add(state.columns); add(state.selectedIndex); add(state.activeFrame)
                    state.frames.forEach { vertices ->
                        vertices.forEach { add(it.position.x); add(it.position.y); add(it.color.toArgb()) }
                    }
                }
            },
            restore = { values ->
                MeshGradientEditorState().apply {
                    rows = values[0] as Int
                    columns = values[1] as Int
                    selectedIndex = values[2] as Int
                    activeFrame = values[3] as Int
                    val count = (rows + 1) * (columns + 1)
                    frames = List(2) { frame ->
                        List(count) { index ->
                            val offset = 4 + (frame * count + index) * 3
                            EditableMeshVertex(Offset(values[offset] as Float, values[offset + 1] as Float),
                                Color(values[offset + 2] as Int))
                        }
                    }
                }
            }
        )

        private fun defaultFrames() = List(2) { frame ->
            List(9) { index ->
                val position = if (frame == 1 && index == 4) Offset(0.68f, 0.35f)
                else Offset((index % 3) / 2f, (index / 3) / 2f)
                EditableMeshVertex(position, MeshPalettes[frame][index])
            }
        }

        private fun midpoint(first: EditableMeshVertex, second: EditableMeshVertex) =
            EditableMeshVertex((first.position + second.position) / 2f, lerp(first.color, second.color, 0.5f))
    }
}
