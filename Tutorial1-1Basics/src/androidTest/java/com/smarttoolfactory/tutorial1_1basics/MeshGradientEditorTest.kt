package com.smarttoolfactory.tutorial1_1basics

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.smarttoolfactory.tutorial1_1basics.chapter6_graphics.MeshGradientEditorState
import com.smarttoolfactory.tutorial1_1basics.chapter6_graphics.Tutorial6_45Screen
import com.smarttoolfactory.tutorial1_1basics.chapter6_graphics.Tutorial6_46Screen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.math.abs

class MeshGradientEditorTest {
    @get:Rule val rule = createComposeRule()

    @Test fun verticesCanBeDraggedAddedRecoloredAndRestored() {
        val restoration = StateRestorationTester(rule)
        restoration.setContent { Tutorial6_45Screen() }
        val initial = captureMesh()
        rule.onNodeWithTag("mesh-preview").performTouchInput {
            swipe(center, center + Offset(width * 0.12f, -height * 0.12f), 500)
        }
        assertTrue("Dragging must change the mesh", difference(initial, captureMesh()) > 0.01f)
        rule.onNodeWithContentDescription("Color Blue").performScrollTo().performClick()
        setSlider("Red", 218f)
        rule.onNodeWithTag("mesh-vertex-color").assertTextEquals("Vertex color · #DA7BE6")
        rule.onNodeWithTag("mesh-add-row").performScrollTo().performClick()
        rule.onNodeWithTag("mesh-add-column").performClick()
        rule.onNodeWithTag("mesh-grid-size").assertTextEquals("4 rows × 4 columns · 16 vertices")
        // The original center is still row 2, column 2 after the insertions.
        rule.onNodeWithTag("mesh-select-vertex").performScrollTo().performClick()
        rule.onNodeWithTag("mesh-vertex-5").performScrollTo().performClick()
        rule.onNodeWithTag("mesh-vertex-color").performScrollTo().assertTextEquals("Vertex color · #DA7BE6")
        val edited = captureMesh()
        saveScreen("editor-phone")
        restoration.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("mesh-grid-size").assertTextEquals("4 rows × 4 columns · 16 vertices")
        rule.onNodeWithTag("mesh-vertex-color").performScrollTo().assertTextEquals("Vertex color · #DA7BE6")
        assertTrue(difference(edited, captureMesh()) < 0.01f)
        rule.onNodeWithTag("reset-mesh").performScrollTo().performClick()
        rule.onNodeWithTag("mesh-grid-size").assertTextEquals("3 rows × 3 columns · 9 vertices")
        assertTrue(difference(initial, captureMesh()) < 0.01f)
    }

    @Test fun keyframeEditsSurviveInsertionAndAnimationPausesAndResumes() {
        val restoration = StateRestorationTester(rule)
        restoration.setContent { Tutorial6_46Screen() }
        rule.onNodeWithTag("mesh-frame-1").performClick()
        setSlider("Vertex X (%)", 60f)
        rule.onNodeWithContentDescription("Color Mint").performScrollTo().performClick()
        rule.onNodeWithTag("mesh-add-row").performScrollTo().performClick()
        rule.onNodeWithTag("mesh-add-column").performClick()
        val end = captureMesh()
        restoration.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("mesh-frame-1").performScrollTo().assertIsSelected()
        assertTrue(difference(end, captureMesh()) < 0.01f)
        rule.onNodeWithTag("mesh-frame-0").performClick()
        val start = captureMesh()
        assertTrue("Frames must be independently editable", difference(start, end) > 0.1f)
        rule.mainClock.autoAdvance = false
        rule.onNodeWithTag("mesh-play-pause").performClick()
        rule.mainClock.advanceTimeBy(2_000)
        rule.onNodeWithTag("mesh-play-pause").performClick()
        rule.mainClock.advanceTimeByFrame()
        val paused = captureMesh()
        assertTrue(difference(start, paused) > 0.05f)
        assertTrue(difference(end, paused) > 0.05f)
        rule.mainClock.advanceTimeBy(1_000)
        assertTrue("Pause must freeze the drawn interpolation", difference(paused, captureMesh()) < 0.001f)
        rule.onNodeWithTag("mesh-play-pause").performClick()
        rule.mainClock.advanceTimeBy(1_000)
        rule.onNodeWithTag("mesh-play-pause").performClick()
        rule.mainClock.advanceTimeByFrame()
        assertTrue(difference(paused, captureMesh()) > 0.02f)
        rule.mainClock.autoAdvance = true
        saveScreen("editor-animated")
        rule.onNodeWithTag("mesh-frame-1").performClick()
        rule.mainClock.advanceTimeByFrame()
        assertTrue("Playback must not overwrite edited endpoints", difference(end, captureMesh()) < 0.01f)
        rule.onNodeWithTag("reset-mesh").performScrollTo().performClick()
        rule.onNodeWithTag("mesh-grid-size").assertTextEquals("3 rows × 3 columns · 9 vertices")
        rule.onNodeWithTag("mesh-frame-0").performScrollTo().assertIsSelected()
    }

    @Test fun topologyInsertionPreservesBothFramesAndBounds() {
        val state = MeshGradientEditorState()
        state.setColor(Color.Red)
        state.moveSelected(Offset(0.7f, 0.4f))
        state.activeFrame = 1
        state.setColor(Color.Blue)
        state.moveSelected(Offset(0.35f, 0.7f))
        val originals = state.frames.map { it.toList() }
        repeat(8) { state.addRow(); state.addColumn() }
        assertEquals(5, state.rows)
        assertEquals(5, state.columns)
        state.frames.forEachIndexed { frame, vertices ->
            assertEquals(36, vertices.size)
            assertTrue("Insertion must preserve every existing vertex", vertices.containsAll(originals[frame]))
            vertices.forEach { assertTrue(it.position.x in 0f..1f && it.position.y in 0f..1f) }
        }
        state.selectedIndex = 0
        state.moveSelected(Offset(0.5f, 0.5f))
        assertEquals(Offset.Zero, state.selectedVertex.position)
        state.selectedIndex = 7
        state.moveSelected(Offset(-100f, 100f))
        val vertices = state.frames[1]
        assertTrue(vertices[7].position.x > vertices[6].position.x)
        assertTrue(vertices[7].position.x < vertices[8].position.x)
        assertTrue(vertices[7].position.y > vertices[1].position.y)
        assertTrue(vertices[7].position.y < vertices[13].position.y)
        assertEquals(originals[0][0], state.frames[0][0])
    }

    @Test fun controlsRemainReachableInShortAndTabletLayouts() {
        val tablet = mutableStateOf(false)
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides if (tablet.value) Density(1.25f) else density) {
                Box(Modifier.height(400.dp)) { Tutorial6_45Screen() }
            }
        }
        rule.onNodeWithTag("Blue").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("mesh-preview").assertIsDisplayed()
        rule.runOnIdle { tablet.value = true }
        rule.onNodeWithTag("Blue").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("mesh-preview").assertIsDisplayed()
        saveScreen("editor-tablet")
        repeat(3) {
            rule.onNodeWithTag("mesh-add-row").performScrollTo().performClick()
            rule.onNodeWithTag("mesh-add-column").performClick()
        }
        rule.onNodeWithTag("mesh-add-row").assertIsNotEnabled()
        rule.onNodeWithTag("mesh-add-column").assertIsNotEnabled()
        rule.onNodeWithTag("reset-mesh").performScrollTo().performClick()
        rule.onNodeWithTag("mesh-add-row").performScrollTo().assertIsEnabled()
    }

    private fun setSlider(tag: String, value: Float) {
        rule.onNodeWithTag(tag).performScrollTo().performSemanticsAction(SemanticsActions.SetProgress) { it(value) }
    }

    private fun captureMesh(): Bitmap = rule.onNodeWithTag("mesh-preview")
        .captureToImage().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, false)

    private fun difference(first: Bitmap, second: Bitmap): Float {
        assertEquals(first.width, second.width)
        assertEquals(first.height, second.height)
        var changed = 0
        var count = 0
        for (y in 8 until first.height - 8 step 16) for (x in 8 until first.width - 8 step 16) {
            val a = first.getPixel(x, y)
            val b = second.getPixel(x, y)
            if (listOf(0, 8, 16).any { abs((a shr it and 255) - (b shr it and 255)) > 6 }) changed++
            count++
        }
        return changed.toFloat() / count
    }

    private fun saveScreen(name: String) {
        val directory = File(InstrumentationRegistry.getInstrumentation().targetContext.filesDir, "mesh-gradients")
        directory.mkdirs()
        File(directory, "$name.png").outputStream().use {
            rule.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
