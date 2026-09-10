@file:OptIn(
    androidx.compose.animation.ExperimentalAnimationApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material.ExperimentalMaterialApi::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class
)

package com.smarttoolfactory.tutorial1_1basics

import android.graphics.Bitmap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.espresso.Espresso.pressBack
import androidx.test.platform.app.InstrumentationRegistry
import com.smarttoolfactory.tutorial1_1basics.chapter6_graphics.Tutorial6_42Screen
import com.smarttoolfactory.tutorial1_1basics.chapter6_graphics.Tutorial6_43Screen
import com.smarttoolfactory.tutorial1_1basics.chapter6_graphics.Tutorial6_44Screen
import com.smarttoolfactory.tutorial1_1basics.tutorial_list.createGraphicsTutorialList
import com.smarttoolfactory.tutorial1_1basics.ui.ComposeTutorialsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.math.abs

class MeshGradientTutorialTest {
    @get:Rule val rule = createComposeRule()

    @Test fun graphicsCardsOpenAndExposeMeshSearchMetadata() {
        rule.setContent {
            val meshes = createGraphicsTutorialList().filter { "MeshGradientPainter" in it.tags }
            assertEquals(5, meshes.size)
            assertTrue(meshes.all { "Mesh Gradient" in it.tags })
            ComposeTutorialsTheme { TutorialNavGraph() }
        }
        rule.onNodeWithTag("category-Graphics").performScrollTo().performClick()
        listOf("6-42 Mesh Gradient Basics", "6-43 Mesh Gradient Control Points",
            "6-44 Animated Mesh Gradient", "6-45 Mesh Gradient Editor",
            "6-46 Animated Mesh Gradient Editor").forEach { title ->
            rule.onNode(hasScrollToIndexAction() and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange) and
                hasAnyAncestor(hasTestTag("tutorials-Graphics")))
                .performScrollToNode(hasText(title))
            rule.onNodeWithText(title).performClick()
            rule.onNodeWithTag("mesh-preview").assertIsDisplayed()
            rule.onNodeWithTag("reset-mesh").performScrollTo().assertIsDisplayed()
            pressBack()
        }
    }

    @Test fun paletteAndControlPointsChangePixelsRestoreAndReset() {
        val restoration = StateRestorationTester(rule)
        restoration.setContent { Tutorial6_43Screen() }
        val initial = captureMesh()
        setSlider("Center X (%)", 67f)
        setSlider("Center Y (%)", 34f)
        assertTrue("Moving the center must reshape the gradient", difference(initial, captureMesh()) > 0.03f)
        rule.onNodeWithText("Lagoon").performScrollTo().performClick()
        rule.onNodeWithTag("Custom Bézier tangents").performScrollTo().performClick()
        setSlider("Tangent bend (%)", 12f)
        rule.onNodeWithTag("Bicubic colors").performScrollTo().performClick()
        val edited = captureMesh()
        assertTrue("Controls must change the rendered mesh", difference(initial, edited) > 0.1f)
        save(edited, "control-points")
        save(rule.onRoot().captureToImage().asAndroidBitmap(), "control-points-screen")
        restoration.emulateSavedInstanceStateRestore()
        rule.onNodeWithText("Lagoon").performScrollTo().assertIsSelected()
        rule.onNodeWithTag("Custom Bézier tangents").performScrollTo().assertIsOn()
        rule.onNodeWithTag("Bicubic colors").performScrollTo().assertIsOff()
        assertTrue("Restored mesh should match", difference(edited, captureMesh()) < 0.01f)
        rule.onNodeWithTag("reset-mesh").performScrollTo().performClick()
        assertTrue("Reset must restore the initial mesh", difference(initial, captureMesh()) < 0.01f)
    }

    @Test fun animationMovesPausesResumesAndResets() {
        rule.mainClock.autoAdvance = false
        rule.setContent { Tutorial6_44Screen() }
        val initial = captureMesh()
        rule.onNodeWithTag("mesh-play-pause").performScrollTo().performClick()
        rule.mainClock.advanceTimeBy(1_500)
        rule.onNodeWithTag("mesh-play-pause").performClick()
        rule.mainClock.advanceTimeByFrame()
        val paused = captureMesh()
        assertTrue("Animation must change pixels", difference(initial, paused) > 0.01f)
        rule.mainClock.advanceTimeBy(1_500)
        assertTrue("Paused mesh must stay still", difference(paused, captureMesh()) < 0.001f)
        rule.onNodeWithTag("mesh-play-pause").performScrollTo().performClick()
        rule.mainClock.advanceTimeBy(1_500)
        rule.onNodeWithTag("mesh-play-pause").performClick()
        rule.mainClock.advanceTimeByFrame()
        assertTrue("Resumed animation must move", difference(paused, captureMesh()) > 0.01f)
        save(captureMesh(), "animated")
        rule.onNodeWithTag("reset-mesh").performScrollTo().performClick()
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithText("Play").assertExists()
        assertTrue("Reset returns to the initial phase", difference(initial, captureMesh()) < 0.01f)
    }

    @Test fun basicMeshRendersInPhoneAndTabletWidths() {
        val tablet = mutableStateOf(false)
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides
                if (tablet.value) Density(1.25f, 1f) else density) {
                Tutorial6_42Screen()
            }
        }
        val phone = captureMesh()
        // A blank/no-op renderer would produce the same color everywhere.
        assertTrue(phone.getPixel(phone.width / 5, phone.height / 5) !=
            phone.getPixel(phone.width * 4 / 5, phone.height * 4 / 5))
        save(phone, "basic-phone")
        save(rule.onRoot().captureToImage().asAndroidBitmap(), "basic-phone-screen")
        rule.runOnIdle { tablet.value = true }
        rule.onNodeWithText("Orchid").performScrollTo().performClick().assertIsSelected()
        save(captureMesh(), "basic-tablet")
        rule.onNodeWithTag("reset-mesh").performScrollTo().performClick()
        rule.onNodeWithText("Sunset").performScrollTo().assertIsSelected()
    }

    private fun setSlider(tag: String, value: Float) {
        rule.onNodeWithTag(tag).performScrollTo().performSemanticsAction(SemanticsActions.SetProgress) { it(value) }
    }

    private fun captureMesh(): Bitmap = rule.onNodeWithTag("mesh-preview")
        .performScrollTo().captureToImage().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, false)

    /** Fraction of sampled pixels that differ visibly, allowing tiny renderer rounding changes. */
    private fun difference(first: Bitmap, second: Bitmap): Float {
        assertEquals(first.width, second.width)
        assertEquals(first.height, second.height)
        var changed = 0
        var samples = 0
        for (y in 8 until first.height - 8 step 16) {
            for (x in 8 until first.width - 8 step 16) {
                val a = first.getPixel(x, y)
                val b = second.getPixel(x, y)
                if (listOf(0, 8, 16).any { shift -> abs((a shr shift and 255) - (b shr shift and 255)) > 6 }) changed++
                samples++
            }
        }
        return changed.toFloat() / samples
    }

    private fun save(bitmap: Bitmap, name: String) {
        val directory = File(InstrumentationRegistry.getInstrumentation().targetContext.filesDir, "mesh-gradients")
        directory.mkdirs()
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
