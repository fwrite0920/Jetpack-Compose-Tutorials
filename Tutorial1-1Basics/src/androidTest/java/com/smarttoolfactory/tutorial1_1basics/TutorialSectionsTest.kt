@file:OptIn(androidx.compose.animation.ExperimentalAnimationApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material.ExperimentalMaterialApi::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.smarttoolfactory.tutorial1_1basics

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.smarttoolfactory.tutorial1_1basics.chapter7_theming.*
import com.smarttoolfactory.tutorial1_1basics.chapter9_animation.*
import com.smarttoolfactory.tutorial1_1basics.chapter10_material3.*
import com.smarttoolfactory.tutorial1_1basics.model.TutorialCategory
import com.smarttoolfactory.tutorial1_1basics.tutorial_list.createTutorialCategories
import com.smarttoolfactory.tutorial1_1basics.tutorial_list.createAnimationTutorialList
import com.smarttoolfactory.tutorial1_1basics.ui.ComposeTutorialsTheme
import com.smarttoolfactory.tutorial1_1basics.ui.Material3TutorialTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.text.DateFormat
import java.util.Calendar

class TutorialSectionsTest {
    @get:Rule val rule = createComposeRule()

    @Test fun allEightTabsRenderCardsAndNewRoutesOpen() {
        rule.setContent { ComposeTutorialsTheme { TutorialNavGraph() } }
        listOf("Components", "Material 3", "Layout", "State", "Gesture", "Graphics", "Theming", "Animation")
            .forEach { category ->
                rule.onNodeWithTag("category-$category").performScrollTo().performClick()
                rule.onNodeWithTag("tutorials-$category").assertIsDisplayed()
                    .assert(hasAnyDescendant(hasClickAction()))
            }
        rule.onNodeWithTag("category-Material 3").performScrollTo().performClick()
        rule.onNodeWithText("10-1 Material 2 vs Material 3 Buttons").performClick()
        rule.onNodeWithTag("Buttons enabled").assertExists()
    }

    @Test fun searchIndexesAllNewTagsAndTitlesAreUnique() {
        val viewModel = HomeViewModel()
        lateinit var categories: List<TutorialCategory>
        rule.setContent {
            categories = createTutorialCategories {}
            viewModel.categories = categories
            ComposeTutorialsTheme { HomeScreen(viewModel = viewModel, navigateToTutorial = {}) }
        }
        rule.runOnIdle {
            val titles = categories.flatMap { it.tutorials }.map { it.title }
            assertEquals(titles.size, titles.toSet().size)
            val animations = categories.first { it.title == "Animation" }.tutorials
            assertTrue(animations.indexOfFirst { it.title.contains("Gooey Union Background") } <
                animations.indexOfFirst { it.title.contains("Shared Elements: List to Detail") })
            assertEquals(listOf(9, 7, 37), listOf("Material 3", "Theming", "Animation")
                .map { tag -> categories.first { it.title == tag }.tutorials.size })
        }
        listOf("Material 3" to "10-1 Material 2 vs Material 3 Buttons",
            "Theming" to "7-1 Theme Anatomy",
            "Animation" to "9-1 Choosing an Animation API").forEach { (query, title) ->
            rule.onNode(hasSetTextAction()).performTextReplacement(query)
            rule.waitUntil(5_000) {
                rule.onAllNodes(hasScrollToIndexAction() and
                    SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
                    .fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNode(hasScrollToIndexAction() and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
                .performScrollToNode(hasText(title))
            rule.onNodeWithText(title).assertIsDisplayed()
            rule.runOnIdle { assertTrue(viewModel.getTutorials(query).any { it.title == title }) }
        }
    }

    @Test fun buttonVariantsRespondAndLoadingDisablesSubmission() {
        rule.setContent { Tutorial10_1Screen() }
        rule.onNodeWithTag("m2-filled").performScrollTo().performClick()
        rule.onNodeWithTag("m3-filled").performScrollTo().performClick()
        rule.onNodeWithTag("button-result").assertTextEquals("Button clicks: 2")
        rule.onNodeWithTag("Loading content").performScrollTo().performClick()
        rule.onNodeWithTag("m3-filled").performScrollTo().assertIsNotEnabled()
        rule.onNodeWithTag("Loading content").performScrollTo().performClick()
        rule.onNodeWithTag("Buttons enabled").performScrollTo().performClick()
        rule.onNodeWithTag("m2-filled").assertIsNotEnabled()
        rule.onNodeWithTag("m3-filled").assertIsNotEnabled()
    }

    @Test fun chipsSelectDisableDeleteAndRestore() {
        rule.setContent { Tutorial10_2Screen() }
        rule.onNodeWithTag("m2-filter").performScrollTo().performClick().assertIsSelected()
        rule.onNodeWithTag("m3-filter").performScrollTo().performClick().assertIsSelected()
        rule.onNodeWithTag("input-chip").performScrollTo().performClick().assertIsSelected()
        rule.onNodeWithContentDescription("Remove Alex").performClick()
        rule.onNodeWithTag("input-chip").assertDoesNotExist()
        rule.onNodeWithText("Restore Alex").performClick()
        rule.onNodeWithTag("input-chip").assertExists().assertIsNotSelected()
        rule.onNodeWithTag("Chips enabled").performScrollTo().performClick()
        rule.onNodeWithTag("m3-filter").assertIsNotEnabled()
    }

    @Test fun segmentedButtonsExposeSingleAndMultipleChoiceState() {
        rule.setContent { Tutorial10_6Screen() }
        rule.onNodeWithTag("single-Week").performClick().assertIsSelected()
        rule.onNodeWithTag("single-Day").assertIsNotSelected()
        rule.onNodeWithTag("single-result").assertTextEquals("Period: Week")
        rule.onNodeWithTag("multiple-Saved").performClick().assertIsOn()
        rule.onNodeWithTag("multiple-Unread").performClick().assertIsOn()
        rule.onNodeWithTag("multiple-result").assertTextEquals("Filters: Saved, Unread")
        rule.onNodeWithTag("multiple-Saved").performClick().assertIsOff()
    }

    @Test fun responsiveNavigationSwitchesAt600AndRestoresSelection() {
        var width by mutableIntStateOf(599)
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            Material3TutorialTheme {
                var selected by rememberSaveable { mutableIntStateOf(0) }
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    ResponsiveNavigationDemo(Modifier.requiredWidth(width.dp).height(400.dp),
                        selected, { selected = it })
                }
            }
        }
        rule.onNodeWithTag("responsive-bar").assertExists()
        rule.onNodeWithTag("responsive-rail").assertDoesNotExist()
        rule.onNodeWithText("Favorites").performClick()
        rule.onNodeWithTag("navigation-result").assertTextEquals("Favorites")
        rule.runOnIdle { width = 600 }
        rule.onNodeWithTag("responsive-rail").assertExists()
        rule.onNodeWithTag("responsive-bar").assertDoesNotExist()
        restoration.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("navigation-result").assertTextEquals("Favorites")
        rule.onNodeWithContentDescription("Create item").performClick()
        rule.onNodeWithText("Created: 1").assertExists()
    }

    @Test fun navigationRailHasItsOwnRouteAndWorksOnPhones() {
        rule.setContent {
            Box(Modifier.widthIn(max = 360.dp)) {
                ComposeTutorialsTheme { TutorialNavGraph() }
            }
        }
        rule.onNode(hasSetTextAction()).performTextInput("NavigationRail")
        rule.waitUntil(5_000) {
            rule.onAllNodesWithText("10-9 Navigation Rail").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("10-3 Navigation Bar").assertDoesNotExist()
        rule.onNodeWithText("10-9 Navigation Rail").performClick()
        rule.onNodeWithTag("standalone-rail").performScrollTo().assertIsDisplayed()
        val inRail = hasAnyAncestor(hasTestTag("standalone-rail"))
        rule.onNode(hasText("Favorites") and inRail).performClick().assertIsSelected()
        rule.onNodeWithTag("rail-result").assertTextEquals("Favorites")
        rule.onNode(hasContentDescription("Create item") and inRail).performClick()
        rule.onNodeWithTag("rail-created").assertTextEquals("Created: 1")
        saveScreenshot("navigation-rail-phone")
        rule.onNodeWithTag("Show navigation FAB").performScrollTo().performClick()
        rule.onNode(hasContentDescription("Create item") and inRail).assertDoesNotExist()
    }

    @Test fun drawerOpensSelectsAndCloses() {
        rule.setContent { Tutorial10_4Screen() }
        rule.onNodeWithText("Open drawer").performScrollTo().performClick()
        rule.onNodeWithText("Saved").performClick()
        rule.onNodeWithTag("drawer-result").assertTextEquals("Saved")
        rule.onNodeWithText("Open drawer").assertIsDisplayed()
        rule.onNodeWithText("Dismissible").performScrollTo().performClick()
        rule.onNodeWithText("Open drawer").performClick()
        rule.onNodeWithText("Settings").performClick()
        rule.onNodeWithTag("drawer-result").assertTextEquals("Settings")
        rule.onNodeWithText("Permanent").performScrollTo().performClick()
        rule.onNodeWithText("Always visible").assertExists()
    }

    @Test fun bothCarouselsRenderScrollAndSelectImages() {
        rule.setContent { Tutorial10_5Screen() }
        rule.onNodeWithTag("uncontained-carousel").assertDoesNotExist()
        rule.onNodeWithContentDescription("Cupcake").performClick()
        rule.onNodeWithTag("carousel-selection").assertTextEquals("Selected image: Cupcake")
        rule.onNodeWithTag("multi-browse-carousel").performTouchInput { swipeLeft() }
        rule.onNodeWithTag("browse-position").assert(SemanticsMatcher("advanced browse position") {
            it.config[SemanticsProperties.Text].single().text != "Multi-browse item: 1"
        })
        rule.onNodeWithText("Next multi-browse").performClick()
        rule.onNodeWithText("Uncontained").performClick()
        rule.onNodeWithTag("multi-browse-carousel").assertDoesNotExist()
        rule.onNodeWithTag("uncontained-carousel").performTouchInput { swipeLeft() }
        rule.onNodeWithTag("uncontained-position").assert(SemanticsMatcher("advanced uncontained position") {
            it.config[SemanticsProperties.Text].single().text != "Uncontained item: 1"
        })
        rule.onNodeWithText("Next uncontained").performClick()
    }

    @Test fun carouselWidthsUpdateThePreviewAndRespectSmallItemBounds() {
        rule.setContent { Tutorial10_5Screen() }
        val initialWidth = rule.onNodeWithContentDescription("Cupcake").fetchSemanticsNode().boundsInRoot.width
        rule.onNodeWithTag("Preferred item width (dp)").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(320f) }
        val largerWidth = rule.onNodeWithContentDescription("Cupcake").fetchSemanticsNode().boundsInRoot.width
        assertTrue("The preferred width changes the live carousel layout", largerWidth > initialWidth)
        rule.onNodeWithTag("Minimum small item width (dp)").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(96f) }
        rule.onNodeWithText("Maximum small item width (dp): 96").assertExists()
        rule.onNodeWithTag("Maximum small item width (dp)").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(160f) }
        rule.onNodeWithTag("Preferred item width (dp)").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(120f) }
        rule.onNodeWithText("Maximum small item width (dp): 112").assertExists()
        rule.onNodeWithTag("multi-browse-carousel").assertIsDisplayed()
        rule.onNodeWithText("Uncontained").performClick()
        rule.onNodeWithTag("Minimum small item width (dp)").assertDoesNotExist()
        rule.onNodeWithTag("Item width (dp)").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(280f) }
        rule.onNodeWithText("Item width (dp): 280").assertExists()
        rule.onNodeWithTag("uncontained-carousel").assertIsDisplayed()
        saveScreenshot("carousel-uncontained-width")
        rule.onNodeWithText("Multi-browse").performClick()
        rule.onNodeWithText("Preferred item width (dp): 120").assertExists()
        rule.onNodeWithText("Reset carousel options").performScrollTo().performClick()
        rule.onNodeWithText("Preferred item width (dp): 186").assertExists()
        rule.onNodeWithText("Minimum small item width (dp): 40").assertExists()
        rule.onNodeWithText("Maximum small item width (dp): 56").assertExists()
    }

    @Test fun carouselControlsRemainReachableInShortWindows() {
        rule.setContent {
            Box(Modifier.height(480.dp)) { Tutorial10_5Screen() }
        }
        rule.onNodeWithTag("Maximum small item width (dp)").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Reset carousel options").performScrollTo().performClick()
        rule.onNodeWithText("Uncontained").performScrollTo().performClick()
        rule.onNodeWithTag("Item width (dp)").performScrollTo().assertIsDisplayed()
    }

    @Test fun searchFiltersClearsSubmitsAndSupportsBothPresentations() {
        rule.setContent { Tutorial10_7Screen() }
        val expanded = hasAnyAncestor(isDialog()) or hasAnyAncestor(isPopup())
        listOf("Full screen", "Docked").forEach { mode ->
            rule.onNodeWithText(mode).performClick()
            rule.onNodeWithTag("m3-search-input").performClick()
            rule.onNode(hasSetTextAction() and expanded).performTextInput("rail")
            rule.onNodeWithTag("search-option-Navigation rail").assertIsDisplayed()
            rule.onNodeWithTag("search-option-Buttons").assertDoesNotExist()
            rule.onNode(hasContentDescription("Clear search") and expanded).performClick()
            rule.onNodeWithTag("search-option-Buttons").assertExists()
            rule.onNode(hasSetTextAction() and expanded).performTextInput("Chips")
            rule.onNodeWithTag("search-option-Chips").performClick()
            rule.onNodeWithTag("search-result").assertTextEquals("Submitted: Chips")
            rule.onNodeWithTag("m3-search-input").performClick()
            rule.onNode(hasSetTextAction() and expanded).performTextReplacement("Buttons")
            rule.onNode(hasSetTextAction() and expanded).performImeAction()
            rule.onNodeWithTag("search-result").assertTextEquals("Submitted: Buttons")
        }
    }

    @Test fun dateRangeAndTimeDialogsConfirmOrDismissDrafts() {
        rule.setContent { Tutorial10_8Screen() }
        rule.onNodeWithText("Choose date").performClick()
        rule.onNodeWithText("Confirm date").assertIsNotEnabled()
        rule.onNodeWithText(calendarDate(15), substring = true).performClick()
        rule.onNodeWithText("Confirm date").performClick()
        rule.onNodeWithTag("date-result").assert(SemanticsMatcher("confirmed date") {
            !it.config[SemanticsProperties.Text].single().text.contains("Not selected")
        })
        rule.onNodeWithText("Choose date range").performClick()
        rule.onNodeWithText("Confirm range").assertIsNotEnabled()
        rule.onNodeWithText(calendarDate(10), substring = true).performClick()
        rule.onNodeWithText(calendarDate(15), substring = true).performClick()
        rule.onNodeWithText("Confirm range").performClick()
        rule.onNodeWithTag("range-result").assert(SemanticsMatcher("confirmed range") {
            !it.config[SemanticsProperties.Text].single().text.contains("Not selected")
        })
        rule.onNodeWithText("Choose dial time").performClick()
        rule.onNodeWithText("Cancel").performClick()
        rule.onNodeWithTag("time-result").assertTextEquals("Time: Not selected")
        rule.onNodeWithText("Choose keyboard time").performClick()
        rule.onAllNodes(hasSetTextAction())[0].performTextReplacement("11")
        rule.onAllNodes(hasSetTextAction())[1].performTextReplacement("45")
        rule.onNodeWithText("Confirm time").performClick()
        rule.onNodeWithTag("time-result").assertTextContains("11:45", substring = true)
        rule.onNodeWithText("Choose dial time").performClick()
        rule.onNodeWithText("Confirm time").performClick()
        rule.onNodeWithTag("time-result").assertTextContains("11:45", substring = true)
    }

    @Test fun animationControlsHaveBreathingRoomWithinSamples() {
        rule.setContent { Tutorial9_2Screen() }
        val reset = rule.onNodeWithTag("reset-AnimatedVisibilityTransitionSample").getUnclippedBoundsInRoot()
        val button = rule.onNodeWithText("Update Target State").getUnclippedBoundsInRoot()
        val state = rule.onNode(hasText("State currentState: false\ntargetState: false\nisIdle:  true") and
            hasAnyAncestor(hasTestTag("animation-demo-AnimatedVisibilityTransitionSample")))
            .getUnclippedBoundsInRoot()
        assertEquals(16f, (button.top - reset.bottom).value, .5f)
        assertEquals(16f, (state.top - button.bottom).value, .5f)
        saveScreenshot("animation-balanced-spacing")
    }

    @Test fun animationSamplesHave24DpBetweenContentAndNextHeading() {
        rule.setContent { Tutorial9_2Screen() }
        val first = rule.onNodeWithTag("animation-demo-AnimatedVisibilityTransitionSample")
        first.assertExists()
        val next = rule.onNodeWithTag("animation-heading-AnimatedVisibilityCloseTest")
        next.assertExists()
        val contentBounds = first.getUnclippedBoundsInRoot()
        val headingBounds = next.getUnclippedBoundsInRoot()
        assertEquals(24f, (headingBounds.top - contentBounds.bottom).value, .5f)
        saveScreenshot("animation-exact-24dp-spacing")
    }

    @Test fun compactAnimationSamplesDoNotReserveAFullViewport() {
        var chapter by mutableIntStateOf(0)
        rule.setContent {
            key(chapter) {
                when (chapter) {
                    0 -> Tutorial9_1Screen()
                    1 -> Tutorial9_2Screen()
                    else -> Tutorial9_3Screen()
                }
            }
        }
        listOf("StateAnimationDemo", "AnimatedVisibilityTransitionSample", "SimpleAnimatedContentSample")
            .forEachIndexed { index, id ->
                rule.runOnIdle { chapter = index }
                showAnimation(id)
                val bounds = rule.onNodeWithTag("animation-demo-$id").getUnclippedBoundsInRoot()
                assertTrue(bounds.bottom - bounds.top < 400.dp)
                saveScreenshot("compact-animation-${index + 1}")
            }
        rule.runOnIdle { chapter = 0 }
        showAnimation("StateAnimationDemo")
        rule.onNodeWithTag("animate-state").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("animated-size").assertTextEquals("Animated size: 160dp")
        rule.onNodeWithTag("animation-demo-StateAnimationDemo").assertHeightIsAtLeast(160.dp)
    }

    @Test fun animationReachesTargetAndResetsWithTestClock() {
        rule.setContent { Tutorial9_1Screen() }
        showAnimation("StateAnimationDemo")
        rule.onNodeWithTag("animate-state").performScrollTo()
        rule.mainClock.autoAdvance = false
        rule.onNodeWithTag("animate-state").performClick()
        rule.mainClock.advanceTimeBy(800)
        rule.onNodeWithTag("animated-size").assertTextEquals("Animated size: 160dp")
        rule.mainClock.autoAdvance = true
        rule.onNodeWithTag("reset-StateAnimationDemo").performScrollTo()
        rule.mainClock.autoAdvance = false
        rule.onNodeWithTag("reset-StateAnimationDemo").performClick()
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithTag("animated-size").assertTextEquals("Animated size: 64dp")
        rule.mainClock.autoAdvance = true
        showAnimation("AnimatableInterruptionDemo")
        rule.onNodeWithTag("start-animation").performScrollTo()
        rule.mainClock.autoAdvance = false
        rule.onNodeWithTag("start-animation").performClick()
        rule.mainClock.advanceTimeBy(6_000)
        rule.onNodeWithTag("animation-status").assertTextEquals("Animation: Finished")
        rule.onNodeWithText("Reverse").performClick()
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithText("Stop").performClick()
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithTag("animation-status").assertTextEquals("Animation: Stopped")
        rule.mainClock.autoAdvance = true
    }

    @Test fun everyAnimationFileOpensAndItsSamplesAppearInOrder() {
        var lesson by mutableIntStateOf(0)
        var tablet by mutableStateOf(false)
        rule.setContent {
            val tutorials = createAnimationTutorialList()
            val density = if (tablet) Density(1.25f) else LocalDensity.current
            CompositionLocalProvider(LocalDensity provides density) {
                key(lesson, tablet) { tutorials[lesson].action?.invoke() }
            }
        }
        val samplesByFile = listOf(
            listOf("StateAnimationDemo", "VisibilityDecisionDemo", "CrossfadeDemo", "CoordinatedTransitionDemo", "RepeatingAnimationDemo", "AnimatableInterruptionDemo"),
            listOf("AnimatedVisibilityTransitionSample", "AnimatedVisibilityCloseTest", "MutableTransitionStatePreview", "PoppingInCardPreview", "AnimateEnterExitSample"),
            listOf("SimpleAnimatedContentSample", "AnimateIncrementDecrementSample", "AnimatedContentSample", "TransitionExtensionAnimatedContentSample", "SlideIntoContainerSample"),
            listOf("MutexMutatorMutexTest1", "MutexMutatorMutexTest2"),
            listOf("EasingCurveDemo", "EasingComparisonDemo"),
            listOf("EasingAnimationPlayground"),
            listOf("ComposeTypeWriterPreview"),
            listOf("LazyRowSnapAndDeleteAnimation"),
            listOf("HorizontalPagerDeleteDemo"),
            listOf("ShakeTest", "SingleParticleTrajectorySample", "ParticleAnimationSample"),
            listOf("SpringAnimationPlayground"),
            listOf("KeyframeAnimationPlayground"),
            listOf("ElasticPressDemo"),
            listOf("ShakeTouchDemo"),
            listOf("RotationAnimationPreview"),
            listOf("WaveFillPreview"),
            listOf("SparkleAnimationPreview"),
            listOf("ShapesDemoBadgeToStarPreview", "ShapesDemoCircleToBurstPreview", "ShapesEditorDemoStarPreview"),
            listOf("ThinkingShapePlayground"),
            listOf("ThinkingShapeDemo"),
            listOf("ThinkingTextDemo"),
            listOf("GooeyBackgroundDemo"),
            listOf("ListToDetailsDemo"),
            listOf("SharedBoundsDemo", "SharedElementScope_CompositionLocal"),
            listOf("AnimatedVisibilitySharedElementShortenedExample"),
            listOf("AnimatedVisibilitySharedElementBlurLayer"),
            listOf("SharedElement_ManualVisibleControl", "SharedElementWithCallerManagedVisibility"),
            listOf("SharedElementApp_BoundsTransformExample"),
            listOf("SharedElement_Clipping"),
            listOf("SharedElementApp_ResizeModeExample"),
            listOf("SharedElement_SkipLookaheadSize"),
            listOf("SharedElementRenderInSharedTransitionScopeOverlay"),
            listOf("SharedElementPlaceholderSizeDemo"),
            listOf("SharedElement_PredictiveBack"),
            listOf("SharedElement_Pager"),
            listOf("SharedElementSheetToScreenDemo"),
            listOf("SharedElementsample2")
        )
        listOf(false, true).forEach { wide ->
            rule.runOnIdle { tablet = wide }
            samplesByFile.forEachIndexed { chapter, samples ->
                rule.runOnIdle { lesson = chapter }
                rule.onNodeWithTag("demo-selector").assertDoesNotExist()
                samples.forEach { id ->
                    showAnimation(id)
                    rule.onNodeWithTag("animation-demo-$id").assertIsDisplayed()
                    if (chapter == 4) {
                        rule.onNode(hasText("Play") and
                            hasAnyAncestor(hasTestTag("animation-demo-$id")))
                            .performScrollTo().assertIsDisplayed()
                    }
                }
                if (chapter == 4 || chapter in 10..23) {
                    saveScreenshot("animation-$chapter-${if (wide) "tablet" else "phone"}")
                }
            }
        }
    }

    @Test fun sharedTransitionsUseOriginalPresentation() {
        var lesson by mutableIntStateOf(22)
        rule.setContent {
            ComposeTutorialsTheme {
                val tutorials = createAnimationTutorialList()
                key(lesson) { tutorials[lesson].action?.invoke() }
            }
        }
        (22..36).forEach { index ->
            rule.runOnIdle { lesson = index }
            rule.onNodeWithText("Reset animation").assertDoesNotExist()
            rule.onNodeWithTag("demo-selector").assertDoesNotExist()
            if (index == 23) {
                showAnimation("SharedElementScope_CompositionLocal")
                rule.onNodeWithTag("animation-demo-SharedElementScope_CompositionLocal").assertIsDisplayed()
            }
            if (index == 26) {
                showAnimation("SharedElementWithCallerManagedVisibility")
                rule.onNodeWithTag("animation-demo-SharedElementWithCallerManagedVisibility").assertIsDisplayed()
            }
        }
    }

    @Test fun placeholderModesOpenAndReturn() {
        rule.setContent { Tutorial9_33Screen() }
        showAnimation("SharedElementPlaceholderSizeDemo")
        listOf("ContentSize", "AnimatedSize").forEach { mode ->
            rule.onNodeWithText(mode).performClick()
            rule.onNodeWithContentDescription("Open Cupcake").performClick()
            rule.onNodeWithText("Back to snacks").performClick()
            rule.onNodeWithContentDescription("Open Cupcake").assertIsDisplayed()
        }
    }

    @Test fun easingCurveScrubsReplaysAndResets() {
        rule.setContent { Material3TutorialTheme { EasingCurveDemo() } }
        rule.onNode(hasText("Back") and hasClickAction()).performClick()
        rule.onNodeWithTag("easing-scrubber").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(.5f) }
        rule.onNodeWithTag("easing-readout").assertTextContains("Time 50%", substring = true)
        rule.onNodeWithText("Reset", substring = false).performScrollTo().performClick()
        rule.onNodeWithTag("easing-readout").assertTextContains("Time 0%", substring = true)
        rule.onNodeWithText("Play", substring = false).performScrollTo()
        rule.mainClock.autoAdvance = false
        rule.onNodeWithText("Play", substring = false).performClick()
        rule.mainClock.advanceTimeBy(2200)
        rule.onNodeWithTag("easing-readout").assertTextContains("Time 100%", substring = true)
        rule.mainClock.autoAdvance = true
        rule.onNode(hasText("Back") and hasClickAction()).performScrollTo()
        saveScreenshot("easing-curve-phone")
    }

    @Test fun easingGallerySelectsNamedCurves() {
        rule.setContent { Material3TutorialTheme { EasingAnimationPlayground(Modifier.fillMaxSize()) } }
        rule.onNodeWithTag("easing-gallery").performScrollTo()
            .performScrollToIndex(9)
        rule.onNodeWithTag("easing-option-9").performClick()
        rule.onNodeWithTag("playground-easing-scrubber").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(.5f) }
        rule.onNodeWithText("Scrub time • 50%").assertExists()
        saveScreenshot("easing-gallery-phone")
    }

    @Test fun themingLocalsAreSeparateAndSamplesAppearConsecutively() {
        var screen by mutableIntStateOf(5)
        rule.setContent {
            key(screen) {
                when (screen) {
                    5 -> Tutorial7_5Screen()
                    6 -> Tutorial7_6Screen()
                    else -> Tutorial7_7Screen()
                }
            }
        }
        rule.onNodeWithTag("demo-selector").assertDoesNotExist()
        rule.onNodeWithTag("Compact spacing").performScrollTo().performClick()
        rule.onNodeWithText("Parent tokens\nSpacing: 8.0.dp • Elevation: 2.0.dp").assertExists()
        rule.onNodeWithText("Scoped spacing • 4dp\nSpacing: 4.0.dp • Elevation: 2.0.dp").assertExists()
        rule.runOnIdle { screen = 6 }
        listOf("Content alpha", "Content color", "Icon locals").forEach {
            rule.onNodeWithText(it).performScrollTo().assertIsDisplayed()
        }
        rule.onNodeWithTag("demo-selector").assertDoesNotExist()
        rule.runOnIdle { screen = 7 }
        rule.onNode(hasText("LocalCounter 0") and hasClickAction()).performScrollTo().performClick()
        rule.onAllNodesWithText("LocalCounter 1").assertCountEquals(2)
        rule.onNode(hasText("LocalStaticCounter 0") and hasClickAction()).performScrollTo().performClick()
        rule.onAllNodesWithText("LocalStaticCounter 1").assertCountEquals(2)
        rule.onNodeWithText("Change Theme").performScrollTo().performClick()
        rule.onNodeWithText("Selected theme: Dark").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("demo-selector").assertDoesNotExist()
    }

    @Test fun newScreensRenderInLightAndDarkThemes() {
        var dark by mutableStateOf(false)
        var tablet by mutableStateOf(false)
        var screen by mutableIntStateOf(0)
        rule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                    if (dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
            }
            val density = if (tablet) Density(1.25f) else LocalDensity.current
            CompositionLocalProvider(LocalConfiguration provides configuration, LocalDensity provides density) {
                key(dark, screen, tablet) {
                    when (screen) {
                        0 -> Tutorial7_1Screen()
                        1 -> Tutorial7_2Screen()
                        2 -> Tutorial7_3Screen()
                        3 -> Tutorial7_4Screen()
                        4 -> Tutorial7_5Screen()
                        5 -> Tutorial10_1Screen()
                        6 -> Tutorial10_3Screen()
                        7 -> Tutorial10_5Screen()
                        8 -> Tutorial10_9Screen()
                        9 -> Tutorial9_6Screen()
                        10 -> Tutorial9_11Screen()
                        11 -> Tutorial9_12Screen()
                        12 -> Tutorial9_19Screen()
                        13 -> Tutorial7_6Screen()
                        14 -> Tutorial7_7Screen()
                    }
                }
            }
        }
        listOf(false to false, false to true, true to false, true to true).forEach { (night, wide) ->
            rule.runOnIdle { dark = night; tablet = wide }
            repeat(15) { index ->
                rule.runOnIdle { screen = index }
                rule.onAllNodes(hasText(when {
                    index < 5 || index >= 13 -> "7-"
                    index < 9 -> "10-"
                    else -> "9-"
                }, substring = true))[0]
                    .assertIsDisplayed()
                if (index == 0) {
                    val layouts = mutableListOf<TextLayoutResult>()
                    rule.onNodeWithText("7-1 Theme Anatomy")
                        .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
                    val luminance = layouts.single().layoutInput.style.color.luminance()
                    assertTrue("Tutorial headings must contrast with the preview surface",
                        if (night) luminance > 0.5f else luminance < 0.2f)
                }
                if (index == 0 || index == 5 || index == 7 || index >= 8) {
                    saveScreenshot("screen-$index-${if (night) "dark" else "light"}-${if (wide) "tablet" else "phone"}")
                }
            }
        }
    }


    @Test fun postedSpringAndKeyframePlaygroundsReplayAndReset() {
        var spring by mutableStateOf(true)
        rule.setContent { key(spring) { if (spring) Tutorial9_11Screen() else Tutorial9_12Screen() } }
        listOf(true, false).forEach { isSpring ->
            rule.runOnIdle { spring = isSpring }
            showAnimation(if (isSpring) "SpringAnimationPlayground" else "KeyframeAnimationPlayground")
            rule.onNodeWithText("Replay").performScrollTo().performClick()
            rule.onNodeWithText(if (isSpring) "Value 1.000" else "Progress 1.00").assertExists()
            rule.onNodeWithText("Reset", useUnmergedTree = true).performScrollTo().performClick()
            rule.onNodeWithText(if (isSpring) "Value 0.000" else "Progress 0.00").assertExists()
        }
    }

    @Test fun postedModifierControlsRespond() {
        var screen by mutableIntStateOf(0)
        rule.setContent { key(screen) {
            when (screen) {
                0 -> Tutorial9_13Screen()
                1 -> Tutorial9_14Screen()
                2 -> Tutorial9_22Screen()
                3 -> Tutorial9_20Screen()
                else -> Tutorial9_21Screen()
            }
        } }
        showAnimation("ElasticPressDemo")
        rule.onNodeWithTag("elastic-button").performScrollTo().performClick()
        rule.onNodeWithTag("elastic-result").assertTextEquals("Clicks: 1")
        rule.runOnIdle { screen = 1 }
        showAnimation("ShakeTouchDemo")
        rule.onNodeWithTag("shake-button").performScrollTo().performTouchInput { click() }
        rule.onNodeWithTag("shake-result").assertTextEquals("Completed: 1")
        rule.runOnIdle { screen = 2 }
        showAnimation("GooeyBackgroundDemo")
        rule.onNodeWithTag("gooey-toggle").performScrollTo().performClick()
        rule.onNodeWithText("Collapse background").assertExists()
        rule.onNodeWithTag("gooey-toggle").performClick()
        rule.onNodeWithText("Expand background").assertExists()
        rule.runOnIdle { screen = 3 }
        showAnimation("ThinkingShapeDemo")
        rule.onNodeWithTag("Run thinking indicator").performScrollTo().performClick().assertIsOn()
        rule.onNodeWithTag("Run thinking indicator").performClick().assertIsOff()
        rule.runOnIdle { screen = 4 }
        showAnimation("ThinkingTextDemo")
        rule.onNodeWithTag("Animate text highlight").performScrollTo().performClick().assertIsOn()
        rule.onNodeWithTag("Animate text highlight").performClick().assertIsOff()
    }

    private fun showAnimation(id: String) {
        if (rule.onAllNodesWithTag("animation-examples").fetchSemanticsNodes().isNotEmpty()) {
            rule.onNodeWithTag("animation-examples")
                .performScrollToNode(hasTestTag("animation-demo-$id"))
        }
    }

    private fun calendarDate(day: Int): String =
        DateFormat.getDateInstance(DateFormat.FULL).format(Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 12)
        }.time)

    private fun saveScreenshot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.filesDir, "tutorial-smoke").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            rule.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
